package com.derbysoft.nuke.dlm.server.initializer;

import com.derbysoft.nuke.dlm.server.codec.PermitResponse2ProtoBufEncoder;
import com.derbysoft.nuke.dlm.server.codec.ProtoBuf2PermitRequestDecoder;
import com.derbysoft.nuke.dlm.server.handler.PermitServerHandler;
import com.derbysoft.nuke.dlm.server.status.StatsCenter;
import com.derbysoft.nuke.dlm.server.status.TrafficMonitorHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by passyt on 16-9-4.
 */
@Component("permitServerTcpInitializer")
public class PermitServerTcpInitializer extends PermitServerInitializer {

    @Autowired
    public PermitServerTcpInitializer(PermitServerHandler handler) {
        super(handler);
    }

    @Override
    public String getType() {
        return TYPE_TCP;
    }

    @Override
    protected void beforeInitChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast("logger", new LoggingHandler(LogLevel.DEBUG))
                .addLast("monitorHandler", new TrafficMonitorHandler(StatsCenter.getInstance().getTcpStats()))
                .addLast("idleStateHandler", new IdleStateHandler(0, 0, 180))
                .addLast("frameDecoder", new ProtobufVarint32FrameDecoder())
                .addLast("protobufDecoder", new ProtobufDecoder(com.derbysoft.nuke.dlm.model.Protobuf.Request.getDefaultInstance()))
                .addLast("permitDecoder", new ProtoBuf2PermitRequestDecoder())

                .addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender())
                .addLast("protobufEncoder", new ProtobufEncoder())
                .addLast("permitEncoder", new PermitResponse2ProtoBufEncoder());
    }

    @Override
    protected void afterInitChannel(SocketChannel socketChannel) throws Exception {
    }

}
