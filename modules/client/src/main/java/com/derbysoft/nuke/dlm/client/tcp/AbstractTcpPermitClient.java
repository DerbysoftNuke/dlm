package com.derbysoft.nuke.dlm.client.tcp;

import com.derbysoft.nuke.dlm.client.tcp.coder.PermitRequest2ProtoBufEncoder;
import com.derbysoft.nuke.dlm.client.tcp.coder.ProtoBuf2PermitResponseDecoder;
import com.derbysoft.nuke.dlm.exception.PermitException;
import com.derbysoft.nuke.dlm.model.IPermitRequest;
import com.derbysoft.nuke.dlm.model.IPermitResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by DT219 on 2016-09-14.
 */
class AbstractTcpPermitClient {

    private Logger log = LoggerFactory.getLogger(AbstractTcpPermitClient.class);

    protected EventLoopGroup group;
    protected Channel channel;
    protected Bootstrap bootstrap;

    private String host;
    private int port;

    private static final Cache<String, ResponseFuture> FUTURES = CacheBuilder.from("expireAfterWrite=1h").build();

    public AbstractTcpPermitClient(String host, int port) throws InterruptedException {
        this.host = host;
        this.port = port;

        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024)
                .option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024)
//                .option(ChannelOption.SO_TIMEOUT, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast("logger", new LoggingHandler(LogLevel.DEBUG))
                                .addLast("frameDecoder", new ProtobufVarint32FrameDecoder())
                                .addLast("protobufDecoder", new ProtobufDecoder(com.derbysoft.nuke.dlm.model.Protobuf.Response.getDefaultInstance()))
                                .addLast("permitDecoder", new ProtoBuf2PermitResponseDecoder())

                                .addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender())
                                .addLast("protobufEncoder", new ProtobufEncoder())
                                .addLast("permitEncoder", new PermitRequest2ProtoBufEncoder())
                                .addLast("handler", new ChannelHandlerAdapter() {

                                    @Override
                                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                        log.warn("Connection is down and reconnecting...");
                                        AbstractTcpPermitClient.this.channel = null;
                                        AbstractTcpPermitClient.this.doConnectIfNecessary(true);
                                    }

                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        IPermitResponse response = (IPermitResponse) msg;
                                        ResponseFuture future = FUTURES.getIfPresent(response.getHeader().getTransactionId());
                                        if (future != null) {
                                            future.set(response);
                                            future.done();
                                        }
                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                        super.exceptionCaught(ctx, cause);
                                        log.error("Exception", cause);
                                    }

                                });
                    }

                });
        doConnectIfNecessary(false);
    }

    protected AbstractTcpPermitClient(Channel channel, EventLoopGroup group, Bootstrap bootstrap) {
        this.channel = channel;
        this.group = group;
        this.bootstrap = bootstrap;
    }

    private void doConnectIfNecessary(boolean async) {
        if (channel != null) {
            return;
        }

        synchronized (group) {
            log.debug("Connecting to {}:{}", host, port);
            if (!async) {
                RuntimeException exception = null;
                for (int i = 0; i < 3; i++) {
                    try {
                        this.channel = bootstrap.connect(host, port).sync().channel();
                        return;
                    } catch (InterruptedException e) {
                        exception = new IllegalStateException(e);
                    }
                }

                throw exception;
            } else {
                bootstrap.connect(host, port).addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            channel = future.channel();
                        } else {
                            log.warn("Connecting to {}:{} failed and retry", host, port);
                            future.channel().eventLoop().schedule(() -> {
                                doConnectIfNecessary(true);
                            }, 3, TimeUnit.SECONDS);
                        }
                    }

                });
            }
        }
    }

    public void shutdown() {
        group.shutdownGracefully();
    }

    protected <RS extends IPermitResponse> IResponseFuture<RS> send(IPermitRequest<RS> request) {
        doConnectIfNecessary(false);

        final ResponseFuture<RS> responseFuture = new ResponseFuture<RS>();
        FUTURES.put(request.getHeader().getTransactionId(), responseFuture);
        channel.writeAndFlush(request);
        return responseFuture;
    }

    protected <RS extends IPermitResponse> RS execute(IPermitRequest<RS> request) {
        IResponseFuture<RS> future = send(request);
        try {
            RS rs = future.get();
            assert request.getHeader().getTransactionId().equals(rs.getHeader().getTransactionId());
            if (rs.getErrorMessage() != null && rs.getErrorMessage().length() > 0) {
                throw new PermitException(rs.getErrorMessage());
            }

            return rs;
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    static class ResponseFuture<RS extends IPermitResponse> implements SetEnableResponseFuture<RS> {

        private final CountDownLatch latch = new CountDownLatch(1);
        private RS response;

        @Override
        public void set(RS rs) {
            this.response = rs;
        }

        public ResponseFuture<RS> done() {
            latch.countDown();
            return this;
        }

        @Override
        public RS get() throws InterruptedException {
            latch.await();
            return response;
        }

        @Override
        public RS get(long timeout, TimeUnit unit) throws InterruptedException {
            latch.await(timeout, unit);
            return response;
        }

    }

}
