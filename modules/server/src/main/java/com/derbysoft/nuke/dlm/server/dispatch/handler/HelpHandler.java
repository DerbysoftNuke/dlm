package com.derbysoft.nuke.dlm.server.dispatch.handler;

import com.derbysoft.nuke.dlm.server.dispatch.IHandler;
import com.derbysoft.nuke.dlm.server.dispatch.RequestMapping;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;

/**
 * Created by passyt on 16-9-22.
 */
@Component
@RequestMapping(uri = "/help")
public class HelpHandler extends TemplateSupport implements IHandler {

    public HelpHandler(ITemplateEngine templateEngine) {
        super(templateEngine);
    }

    @Override
    public String execute(String uri, HttpMethod method, String request) {
        return executeWith("help", null);
    }

}
