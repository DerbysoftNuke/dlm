package com.derbysoft.nuke.dlm.server.dispatch;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Created by passyt on 16-9-22.
 */
public interface IDispatcher {

    DefaultFullHttpResponse invoke(FullHttpRequest request);

}
