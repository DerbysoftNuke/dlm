package com.derbysoft.nuke.dlm.server.codec;

import com.derbysoft.nuke.dlm.model.*;
import com.derbysoft.nuke.dlm.server.dispatch.IDispatcher;
import com.google.common.base.Splitter;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <ul>
 * <li>/register/${resourceId}/permitname/${permitname}/spec/${spec}</li>
 * <li>/unregister/${resourceId}</li>
 * <li>/existing/${resourceId}</li>
 * <li>/permit/${resourceId}/action/acquire</li>
 * <li>/permit/${resourceId}/action/tryacquire</li>
 * <li>/permit/${resourceId}/action/tryacquire/timeout/10/timeunit/seconds</li>
 * <li>/permit/${resourceId}/action/release</li>
 * </ul>
 * Created by passyt on 16-9-4.
 */
public class Http2PermitRequestDecoder extends MessageToMessageDecoder<FullHttpRequest> {

    private final Logger log = LoggerFactory.getLogger(Http2PermitRequestDecoder.class);

    public static final String METHOD_REGISTER = "register";
    public static final String METHOD_UNREGISTER = "unregister";
    public static final String METHOD_EXISTING = "existing";
    public static final String METHOD_PERMIT = "permit";

    public static final String ACTION_ACQUIRE = "acquire";
    public static final String ACTION_TRYACQUIRE = "tryacquire";
    public static final String ACTION_RELEASE = "release";

    public static final String PARAMETER_ACTION = "action";
    public static final String PARAMETER_PERMITNAME = "permitname";
    public static final String PARAMETER_SPEC = "spec";
    public static final String PARAMETER_TIMEOUT = "timeout";
    public static final String PARAMETER_TIMEUNIT = "timeunit";

    private final IDispatcher dispatcher;

    public Http2PermitRequestDecoder(IDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) throws Exception {
        if (!request.decoderResult().isSuccess()) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST)).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        Header header = new Header(request.headers().getAndConvert("transactionId", UUID.randomUUID().toString().replace("-", "")));
        String uri = request.uri();
        log.debug("Request uri: {} from remote {}", uri, ctx.channel().remoteAddress());

        Map<String, String> parameters = toParameters(uri.substring(1));
        if (parameters.containsKey(METHOD_REGISTER)) {
            String resourceId = optional(parameters, METHOD_REGISTER);
            out.add(new RegisterRequest(resourceId, required(parameters, PARAMETER_PERMITNAME), optional(parameters, PARAMETER_SPEC), header));
            return;
        } else if (parameters.containsKey(METHOD_UNREGISTER)) {
            String resourceId = optional(parameters, METHOD_UNREGISTER);
            out.add(new UnRegisterRequest(resourceId, header));
            return;
        } else if (parameters.containsKey(METHOD_EXISTING)) {
            String resourceId = optional(parameters, METHOD_EXISTING);
            out.add(new ExistingRequest(resourceId, header));
            return;
        } else if (parameters.containsKey(METHOD_PERMIT)) {
            String resourceId = optional(parameters, METHOD_PERMIT);
            String action = optional(parameters, PARAMETER_ACTION);
            if (ACTION_ACQUIRE.equals(action)) {
                out.add(new AcquireRequest(resourceId, header));
                return;
            } else if (ACTION_TRYACQUIRE.equals(action)) {
                if (parameters.containsKey(PARAMETER_TIMEOUT) && parameters.containsKey(PARAMETER_TIMEUNIT)) {
                    out.add(new TryAcquireRequest(resourceId, Long.parseLong(required(parameters, PARAMETER_TIMEOUT)), TimeUnit.valueOf(required(parameters, PARAMETER_TIMEUNIT).toUpperCase()), header));
                } else {
                    out.add(new TryAcquireRequest(resourceId, header));
                }
                return;
            } else if (ACTION_RELEASE.equals(action)) {
                out.add(new ReleaseRequest(resourceId, header));
                return;
            }
        }

        DefaultFullHttpResponse response = dispatcher.invoke(request);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static Map<String, String> toParameters(String string) {
        String key = null;
        Map<String, String> parameters = new HashMap<>();
        for (Iterator<String> it = Splitter.on("/").split(string).iterator(); it.hasNext(); ) {
            String value = it.next();
            if (key == null) {
                key = value;
            } else {
                parameters.put(key, value);
                key = null;
            }
        }

        if (key != null && !parameters.containsKey(key)) {
            parameters.put(key, null);
        }
        return parameters;
    }

    protected String required(Map<String, String> parameters, String key) {
        String value = optional(parameters, key);
        if (value == null) {
            throw new IllegalArgumentException(key + " is required");
        }

        return value;
    }

    protected String optional(Map<String, String> parameters, String key) {
        try {
            return URLDecoder.decode(parameters.get(key), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Invalid key " + key + " by values " + parameters, e);
        }
    }

}
