package com.derbysoft.nuke.dlm.server.handler;

import com.derbysoft.nuke.dlm.IPermitService;
import com.derbysoft.nuke.dlm.model.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static com.derbysoft.nuke.dlm.model.Protobuf.Response.ResponseType.PING_RESPONSE;
import static com.derbysoft.nuke.dlm.server.initializer.PermitServerInitializer.TYPE_TCP;


/**
 * Created by passyt on 16-9-2.
 */
@ChannelHandler.Sharable
@Component
public class PermitServerHandler extends ChannelHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(PermitServerHandler.class);
    private IPermitService permitService;
    private Executor executor = Executors.newCachedThreadPool();
    private final LoadingCache<Channel, LoadingCache<String, AtomicLong>> acquiredResourceIds = CacheBuilder.newBuilder().build(new CacheLoader<Channel, LoadingCache<String, AtomicLong>>() {

        @Override
        public LoadingCache<String, AtomicLong> load(Channel key) throws Exception {
            return CacheBuilder.newBuilder().build(new CacheLoader<String, AtomicLong>() {
                @Override
                public AtomicLong load(String resourceId) throws Exception {
                    return new AtomicLong(0);
                }
            });
        }

    });

    @Autowired
    public PermitServerHandler(IPermitService permitService) {
        this.permitService = permitService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Logger streamLog = LoggerFactory.getLogger("http.StreamLog");
        IPermitRequest request = (IPermitRequest) msg;
        executor.execute(() -> {
            streamLog.info("Receive request <<| {} from {}", request, ctx.channel().remoteAddress().toString());
            IPermitResponse response = null;
            boolean granted = false;
            try {
                if (request instanceof ReleaseRequest && (acquiredResourceIds.getIfPresent(ctx.channel()) == null || acquiredResourceIds.getIfPresent(ctx.channel()).getIfPresent(request.getResourceId()) == null)) {
                    //connection is down
                    response = request.newResponse();
                    response.setResourceId(request.getResourceId());
                    response.setHeader(request.getHeader());
                } else {
                    response = permitService.execute(request);
                }

                if (response instanceof AcquireResponse) {
                    granted = true;
                    log.debug("release permit from {} on resource", ctx.channel().remoteAddress().toString(), request.getResourceId());
                    acquiredResourceIds.get(ctx.channel()).get(request.getResourceId()).incrementAndGet();
                } else if (response instanceof TryAcquireResponse) {
                    if (((TryAcquireResponse) response).isSuccessful()) {
                        granted = true;
                        acquiredResourceIds.get(ctx.channel()).get(request.getResourceId()).incrementAndGet();
                    }
                } else if (response instanceof ReleaseRequest) {
                    acquiredResourceIds.get(ctx.channel()).get(request.getResourceId()).decrementAndGet();
                }
            } catch (Exception e) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));

                log.error("Catch exception by request [" + request + "]", e);
                response = request.newResponse();
                response.setResourceId(request.getResourceId());
                response.setHeader(request.getHeader());
                response.setErrorMessage(writer.toString());
            }

            boolean finalGranted = granted;
            IPermitResponse finalResponse = response;
            ctx.writeAndFlush(response).addListener(future -> {
                try {
                    future.get();
                    streamLog.info("Return response >>| {} to {}", finalResponse, ctx.channel().remoteAddress().toString());
                } catch (Exception e) {
                    if (e.getCause() instanceof ClosedChannelException && finalGranted) {
                        log.warn("release permit from {} with resource {} as connection is down", ((ChannelFuture) future).channel(), request.getResourceId());
                        permitService.execute(new ReleaseRequest(request.getResourceId()));
                    } else {
                        log.error("Unexpected exception", e.getCause());
                    }
                }
            });
        });
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        //ignore http
        if (!ctx.hasAttr(AttributeKey.valueOf(TYPE_TCP))) {
            return;
        }

        //release all permits if socket is broken for TCP
        LoadingCache<String, AtomicLong> als = acquiredResourceIds.getIfPresent(ctx.channel());
        if (als != null) {
            for (String resourceId : als.asMap().keySet()) {
                long total = als.getIfPresent(resourceId).get();
                log.warn("releasing {} permits from {} on resource {} as it's disconnected", total, ctx.channel(), resourceId);
                for (int i = 0; i < total; i++) {
                    log.debug("release permit from {} on resource {}", ctx.channel().remoteAddress().toString(), resourceId);
                    permitService.execute(new ReleaseRequest(resourceId));
                }
            }
        }
        acquiredResourceIds.invalidate(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Catch exception", cause);
        //TODO return back error message
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!(evt instanceof IdleStateEvent)) {
            return;
        }

        IdleStateEvent event = (IdleStateEvent) evt;
        if (event.state() == IdleState.READER_IDLE) {
            log.warn("Reader idle and closing {}", ctx.channel().remoteAddress().toString());
            ctx.close();
        } else if (event.state() == IdleState.WRITER_IDLE) {
        } else if (event.state() == IdleState.ALL_IDLE) {
            log.debug("Ping client {}", ctx.channel().remoteAddress().toString());
            ctx.writeAndFlush(Protobuf.Response.newBuilder()
                    .setType(PING_RESPONSE)
                    .setPingResponse(
                            Protobuf.PingResponse.newBuilder()
                                    .setEcho("Hello")
                    )
                    .build());
        }
    }
}
