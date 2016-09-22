package com.derbysoft.nuke.dlm.server.dispatch.handler;

import com.derbysoft.nuke.dlm.server.dispatch.IHandler;
import com.derbysoft.nuke.dlm.server.dispatch.RequestMapping;
import com.derbysoft.nuke.dlm.server.status.StatsCenter;
import com.google.common.collect.ImmutableMap;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;

import java.util.Map;

/**
 * Created by passyt on 16-9-22.
 */
@Component
@RequestMapping(uri = "/status")
public class StatusHandler extends TemplateSupportHandler implements IHandler {

    public StatusHandler(ITemplateEngine templateEngine) {
        super(templateEngine);
    }

    @Override
    public String execute(String uri, HttpMethod method, String request) {
        Map<String, StatsCenter.TrafficStats> traffics = ImmutableMap.of("HTTP", StatsCenter.getInstance().getHttpStats(), "TCP", StatsCenter.getInstance().getTcpStats());
        return executeWith("status", ImmutableMap.of("traffics", traffics, "permitStats", StatsCenter.getInstance().permitStats()));
    }

}
