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
@RequestMapping(uri = "/status.rpc", contentType = "text/json")
public class StatusHandlerRpc extends JsonRpcSupportHandler {

    @Override
    public Object doExecute(String method, List params) {
        List<String> requests = (List<String>) params.get(0);
        if ("statusWith".equals(method)) {
            System.out.println(StatusCenter.get().statusWith(Sets.newHashSet(requests)));
            return StatusCenter.get().statusWith(Sets.newHashSet(requests));
        }
        return new UnsupportedOperationException(method + " is not supported");
    }
}
