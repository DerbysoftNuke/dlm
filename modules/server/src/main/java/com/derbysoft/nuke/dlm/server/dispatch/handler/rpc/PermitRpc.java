package com.derbysoft.nuke.dlm.server.dispatch.handler.rpc;

import com.derbysoft.nuke.dlm.PermitSpec;
import com.derbysoft.nuke.dlm.server.PermitManager;
import com.derbysoft.nuke.dlm.server.dispatch.RequestMapping;
import com.derbysoft.nuke.dlm.server.dispatch.handler.JsonRpcSupportHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by passyt on 2016/11/27.
 */
@Component("permit.rpc")
@RequestMapping(uri = "/permit.rpc", contentType = "text/json")
public class PermitRpc extends JsonRpcSupportHandler {

    private PermitManager manager;

    @Autowired
    public PermitRpc(PermitManager manager) {
        this.manager = manager;
    }

    @Override
    public Object doExecute(String method, List params) {
        if ("update".equals(method)) {
            if (params != null && params.size() == 3) {
                String resourceId = (String) params.get(0);
                String permitName = (String) params.get(1);
                String permitSpec = (String) params.get(2);
                return manager.update(resourceId, permitName, new PermitSpec(permitSpec));
            }

            throw new IllegalArgumentException("params is invalid");
        }

        throw new UnsupportedOperationException(method);
    }

}
