package com.derbysoft.nuke.dlm.server.dispatch.handler.ci;

import com.derby.nuke.common.module.file.IFileService;
import com.derby.nuke.common.module.rpc.log.LogService;
import com.derbysoft.nuke.dlm.server.dispatch.RequestMapping;
import com.derbysoft.nuke.dlm.server.dispatch.handler.JsonRpcSupportHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by DT219 on 2016-09-22.
 */
@Component("log.ci")
@RequestMapping(uri = "/log.ci", contentType = "text/json")
public class Log extends JsonRpcSupportHandler {

    private final LogService logService;
    private final IFileService fileService;

    @Autowired
    public Log(LogService logService, IFileService fileService) {
        this.logService = logService;
        this.fileService = fileService;
    }

    @Override
    public Object doExecute(String method, List params) {
        if ("listLogs".equals(method)) {
            return logService.listLogs();
        } else if ("tail".equals(method)) {
            return logService.tail((String) params.get(0), (Integer) params.get(1));
        } else if ("lines".equals(method)) {
            return logService.lines((String) params.get(0));
        } else if ("isAppend".equals(method)) {
            return logService.isAppend((String) params.get(0), (Integer) params.get(1));
        } else if ("next".equals(method)) {
            return logService.next((String) params.get(0), (Integer) params.get(1), (Integer) params.get(2));
        } else if ("find".equals(method)) {
            return logService.find((String) params.get(0), (String) params.get(1), (Integer) params.get(2));
        } else if ("findLines".equals(method)) {
            return logService.findLines((String) params.get(0), (String) params.get(1));
        } else if ("findNext".equals(method)) {
            return logService.findNext((String) params.get(0), (String) params.get(1), (Integer) params.get(2), (Integer) params.get(3));
        } else if ("deepFindLines".equals(method)) {
            return logService.findLines((String) params.get(0), (List) params.get(1));
        } else if ("deepFindNext".equals(method)) {
            return logService.findNext((String) params.get(0), (List) params.get(1), (Integer) params.get(2), (Integer) params.get(3));
        } else if ("listDirectories".equals(method)) {
            return fileService.listDirectories((String) params.get(0));
        } else if ("listFiles".equals(method)) {
            return fileService.listFiles((String) params.get(0));
        }
        throw new UnsupportedOperationException(method + " is not supported");
    }

}
