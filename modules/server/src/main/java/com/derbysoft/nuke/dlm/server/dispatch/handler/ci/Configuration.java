package com.derbysoft.nuke.dlm.server.dispatch.handler.ci;

import com.derbysoft.nuke.dlm.server.config.DefaultConfigurer;
import com.derbysoft.nuke.dlm.server.dispatch.RequestMapping;
import com.derbysoft.nuke.dlm.server.dispatch.handler.JsonRpcSupportHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by passyt on 16-9-22.
 */
@Component("configuration.ci")
@RequestMapping(uri = "/configuration.ci", contentType = "text/json")
public class Configuration extends JsonRpcSupportHandler {

    @Autowired
    private DefaultConfigurer configurer;

    @Override
    public Object doExecute(String method, List params) {
        if ("getAll".equals(method)) {
            return configurer.getProperties();
        } else if ("get".equals(method)) {
            return configurer.getProperty((String) params.get(0));
        } else if ("update".equals(method)) {
            return configurer.setProperty((String) params.get(0), (String) params.get(1));
        }

        throw new UnsupportedOperationException(method + " is not supported");
    }

}
