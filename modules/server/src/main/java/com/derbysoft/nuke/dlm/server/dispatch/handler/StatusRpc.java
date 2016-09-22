package com.derbysoft.nuke.dlm.server.dispatch.handler;

import com.derby.nuke.common.module.rpc.status.StatusCenter;
import com.derbysoft.nuke.dlm.server.dispatch.RequestMapping;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by DT219 on 2016-09-22.
 */
@Component
@RequestMapping(uri = "/status.ci", contentType = "text/json")
public class StatusRpc extends JsonRpcSupportHandler {

    @Override
    public Object doExecute(String method, List params) {
        if ("keys".equals(method)) {
            return StatusCenter.get().keys();
        } else if ("status".equals(method)) {
            return StatusCenter.get().status();
        } else if ("statusWith".equals(method)) {
            return StatusCenter.get().statusWith(Sets.newHashSet((List) params.get(0)));
        } else if ("statusWithout".equals(method)) {
            return StatusCenter.get().statusWithout(Sets.newHashSet((List) params.get(0)));
        }
        return new UnsupportedOperationException(method + " is not supported");
    }

}
