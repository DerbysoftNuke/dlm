package com.derbysoft.nuke.dlm.server.dispatch.handler;

import com.derby.nuke.common.module.rpc.log.LinuxLogService;
import com.derby.nuke.common.module.rpc.log.LogService;
import com.derbysoft.nuke.dlm.server.dispatch.RequestMapping;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by DT219 on 2016-09-22.
 */
@Component
@RequestMapping(uri = "/log.rpc", contentType = "text/json")
public class LogHandlerRpc extends JsonRpcSupportHandler {

    private LogService logService = new LinuxLogService("D:\\usr\\local\\logs");

    @Override
    public Object doExecute(String method, List params) {
        if ("listLogs".equals(method)) {
            return logService.listLogs();
        }
        return new UnsupportedOperationException(method + " is not supported");
    }

}
