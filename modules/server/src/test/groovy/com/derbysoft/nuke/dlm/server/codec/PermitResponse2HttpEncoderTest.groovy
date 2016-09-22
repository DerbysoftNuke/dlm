package com.derbysoft.nuke.dlm.server.codec

import com.alibaba.fastjson.JSON
import com.derbysoft.nuke.dlm.model.Header
import com.derbysoft.nuke.dlm.model.RegisterResponse
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by passyt on 16-9-14.
 */
class PermitResponse2HttpEncoderTest extends Specification {

    def PermitResponse2HttpEncoder encoder;
    def ChannelHandlerContext ctx;
    @Shared
    def resourceId = UUID.randomUUID().toString();
    @Shared
    def transactionId = UUID.randomUUID().toString();

    def setup() {
        encoder = new PermitResponse2HttpEncoder();
        ctx = Mock();
    }

    void encode() {
        given:
        def out = [];
        def channelFuture = Mock(ChannelFuture);
        def actualResponse = null;
        def permitResponse = new RegisterResponse(resourceId, true, new Header(transactionId));

        when:
        encoder.encode(ctx, permitResponse, out);

        then:
        1 * ctx.writeAndFlush(_) >> {
            actualResponse = it[0];
            channelFuture
        }
        1 * channelFuture.addListener(ChannelFutureListener.CLOSE)

        def expectedResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(JSON.toJSONBytes(permitResponse)));
        expectedResponse.headers().add("Content-Type", "application/json; charset=utf-8");
        expectedResponse.headers().add("Server", "Netty-5.0");

        expect:
        expectedResponse == actualResponse;
    }

    void exceptionCaught() {
        given:
        def cause = new NullPointerException();
        def channelFuture = Mock(ChannelFuture);
        def actualResponse = null;

        when:
        encoder.exceptionCaught(ctx, cause);

        then:
        1 * ctx.writeAndFlush(_) >> {
            actualResponse = it[0];
            channelFuture
        }
        1 * channelFuture.addListener(ChannelFutureListener.CLOSE)

        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));

        def expectedResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer(writer.toString().getBytes("UTF-8")));
        expectedResponse.headers().add("Content-Type", "application/json; charset=utf-8");
        expectedResponse.headers().add("Server", "Netty-5.0");

        expect:
        expectedResponse == actualResponse;
    }

}
