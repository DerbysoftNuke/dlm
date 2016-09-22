package com.derbysoft.nuke.dlm.server.dispatch;

import io.netty.handler.codec.http.HttpMethod;

/**
 * Created by passyt on 16-9-22.
 */
public interface IHandler {

    String execute(String uri, HttpMethod method, String request);

}
