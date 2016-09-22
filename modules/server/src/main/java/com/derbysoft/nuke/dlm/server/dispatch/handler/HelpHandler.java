package com.derbysoft.nuke.dlm.server.dispatch.handler;

import com.derbysoft.nuke.dlm.server.dispatch.IHandler;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;

/**
 * Created by passyt on 16-9-22.
 */
@Component
public class HelpHandler extends TemplateSupportHandler implements IHandler {

    public HelpHandler(ITemplateEngine templateEngine) {
        super(templateEngine);
    }

    @Override
    public String execute(String uri, HttpMethod method, String request) {
        return executeWith("help", null);
    }

}
