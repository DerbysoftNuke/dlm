package com.derbysoft.nuke.dlm.server.codec

import com.derbysoft.nuke.dlm.model.*
import com.derbysoft.nuke.dlm.server.dispatch.IDispatcher
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.DecoderResult
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaders
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.TimeUnit

/**
 * Created by passyt on 16-9-14.
 */
class Http2PermitRequestDecoderTest extends Specification {

    def Http2PermitRequestDecoder decoder;
    def FullHttpRequest request;
    def ChannelHandlerContext ctx;
    def IDispatcher dispatcher;
    @Shared
    def resourceId = UUID.randomUUID().toString();
    @Shared
    def transactionId = UUID.randomUUID().toString();

    def setup() {
        dispatcher = Mock();
        decoder = new Http2PermitRequestDecoder(dispatcher);
        request = Mock();
        ctx = Mock();
    }

    void "decode permit request"() {
        given:
        def out = [];
        def decoderResult = Mock(DecoderResult);
        def httpHeaders = Mock(HttpHeaders)
        def channel = Mock(Channel);

        when:
        decoder.decode(ctx, request, out);

        then:
        1 * request.decoderResult() >> decoderResult
        1 * ctx.channel() >> channel
        1 * channel.remoteAddress() >> null
        1 * decoderResult.isSuccess() >> true
        1 * request.headers() >> httpHeaders
        1 * httpHeaders.getAndConvert("transactionId", _) >> transactionId
        1 * request.uri() >> uri

        expect:
        out.size() == 1
        out.get(0).class == permitRequest.class
        out.get(0) == permitRequest

        where:
        uri                                                                  | permitRequest
        "/register/${resourceId}/permitname/TestPermit/spec/TestSpec"        | new RegisterRequest(resourceId, "TestPermit", "TestSpec", new Header(transactionId))
        "/unregister/${resourceId}"                                          | new UnRegisterRequest(resourceId, new Header(transactionId))
        "/existing/${resourceId}"                                            | new ExistingRequest(resourceId, new Header(transactionId))
        "/permit/${resourceId}/action/acquire"                               | new AcquireRequest(resourceId, new Header(transactionId))
        "/permit/${resourceId}/action/tryacquire"                            | new TryAcquireRequest(resourceId, new Header(transactionId))
        "/permit/${resourceId}/action/tryacquire/timeout/1/timeunit/seconds" | new TryAcquireRequest(resourceId, 1L, TimeUnit.SECONDS, new Header(transactionId))
        "/permit/${resourceId}/action/release"                               | new ReleaseRequest(resourceId, new Header(transactionId))
    }

}
