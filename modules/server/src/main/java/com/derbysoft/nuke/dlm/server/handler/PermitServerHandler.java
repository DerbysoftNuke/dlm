package com.derbysoft.nuke.dlm.server.handler;

import com.derbysoft.nuke.dlm.IPermitService;
import com.derbysoft.nuke.dlm.model.IPermitRequest;
import com.derbysoft.nuke.dlm.model.IPermitResponse;
import com.derbysoft.nuke.dlm.model.Protobuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.derbysoft.nuke.dlm.model.Protobuf.Response.ResponseType.PING_RESPONSE;


/**
 * Created by passyt on 16-9-2.
 */
@ChannelHandler.Sharable
@Component
public class PermitServerHandler extends ChannelHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(PermitServerHandler.class);
    private IPermitService permitService;
    private Executor executor = Executors.newCachedThreadPool();

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
            try {
                response = permitService.execute(request);
            } catch (Exception e) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));

                log.error("Catch exception by request [" + request + "]", e);
                response = request.newResponse();
                response.setResourceId(request.getResourceId());
                response.setHeader(request.getHeader());
                response.setErrorMessage(writer.toString());
            }
            streamLog.info("Return response >>| {} to {}", response, ctx.channel().remoteAddress().toString());
            ctx.writeAndFlush(response);
        });
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
