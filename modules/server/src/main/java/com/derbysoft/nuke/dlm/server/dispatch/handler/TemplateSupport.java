package com.derbysoft.nuke.dlm.server.dispatch.handler;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * Created by passyt on 16-9-22.
 */
public abstract class TemplateSupport {

    protected final ITemplateEngine templateEngine;

    public TemplateSupport(ITemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    protected String executeWith(String templateName, Map<String, Object> variables) {
        Context context = new Context(Locale.getDefault());
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

}
