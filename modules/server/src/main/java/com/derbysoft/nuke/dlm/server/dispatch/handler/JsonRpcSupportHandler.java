package com.derbysoft.nuke.dlm.server.dispatch.handler;

import com.alibaba.fastjson.JSON;
import com.derbysoft.nuke.dlm.server.dispatch.IHandler;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by passyt on 16-9-22.
 */
public abstract class JsonRpcSupportHandler implements IHandler {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public String execute(String uri, HttpMethod httpMethod, String text) {
        Map response = new HashMap();
        try {
            if (text == null || text.length() == 0) {
                throw new IllegalArgumentException("Invalid request");
            }

            Map request = JSON.parseObject(text, Map.class);
            String method = (String) request.get("method");
            List params = (List) request.get("params");
            Object result = doExecute(method, params);
            response.put("result", result);
        } catch (Exception e) {
            log.error("Invoke json rpc failed by uri " + uri + ", and request " + text, e);
            Map error = new HashMap();
            error.put("type", e.getClass().getName());
            error.put("message", e.toString());
            response.put("error", error);
        }
        return JSON.toJSONString(response);
    }

    public abstract Object doExecute(String method, List params);
}
