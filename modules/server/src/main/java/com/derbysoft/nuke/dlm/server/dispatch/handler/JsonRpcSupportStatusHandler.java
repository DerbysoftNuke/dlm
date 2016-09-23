package com.derbysoft.nuke.dlm.server.dispatch.handler;

import com.alibaba.fastjson.JSON;
import com.derbysoft.nuke.dlm.server.dispatch.IHandler;
import com.derbysoft.nuke.dlm.server.dispatch.RequestMapping;
import com.derbysoft.nuke.dlm.server.status.StatsCenter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.ITemplateEngine;

import java.util.List;
import java.util.Map;

/**
 * Created by DT219 on 2016-09-23.
 */
@Component
@RequestMapping(uri = "/status.rpc")
public class JsonRpcSupportStatusHandler implements IHandler {

    @Override
    public String execute(String uri, HttpMethod method, String request) {
        Map<String,Object> map = getTrafficStatus(ImmutableMap.of("HTTP", StatsCenter.getInstance().getHttpStats(), "TCP", StatsCenter.getInstance().getTcpStats()));
        map.put("permitStats",getPermitStatuses());
        return JSON.toJSONString(map);
    }

    private Map<String,Object> getTrafficStatus(Map<String, StatsCenter.TrafficStats> traffics){
        if(CollectionUtils.isEmpty(traffics.values())){
            return Maps.newHashMap();
        }
        Map<String,Object> map = Maps.newHashMap();
        for(Map.Entry<String,StatsCenter.TrafficStats> entry : traffics.entrySet()){
            String type = entry.getKey();
            StatsCenter.TrafficStats stats = entry.getValue();
            Map<String,Object> trafficSttus = Maps.newHashMap();
            trafficSttus.put("peakConnections", stats.getTraffic().getPeak().getCount());
            trafficSttus.put("peakTimestamp",stats.getTraffic().getPeak().getTimestamp());
            trafficSttus.put("currentActiveConnections",stats.getTraffic().getActives());
            trafficSttus.put("lastAccessTimestamp",stats.getTraffic().getLastTimestamp());

            List<Map<String,Object>> activeConnectionses = Lists.newArrayList();
            for (Channel channel : stats.getChannels()) {
                Map<String,Object> each = Maps.newHashMap();
                each.put("id",channel.id());
                each.put("remoteAddress",channel.remoteAddress());
                each.put("localAddress",channel.localAddress());
                each.put("active",channel.isActive());
                each.put("open",channel.isOpen());
                each.put("connectTimeout",channel.config().getConnectTimeoutMillis());
                activeConnectionses.add(each);
            }
            trafficSttus.put(type+ "ActiveConnections",activeConnectionses);
            map.put(type,trafficSttus);
        }
        return map;
    }

    private List<Map<String, Object>> getPermitStatuses() {
        if (CollectionUtils.isEmpty(StatsCenter.getInstance().permitStats())) {
            return Lists.newArrayList();
        }
        List<Map<String, Object>> permitStatuses = Lists.newArrayList();
        for (Map.Entry<String, StatsCenter.PermitStats> entry : StatsCenter.getInstance().permitStats().entrySet()) {
            Map<String, Object> each = Maps.newHashMap();
            String key = entry.getKey();
            StatsCenter.PermitStats permitStats = entry.getValue();
            each.put("resource", key);
            each.put("permitName", permitStats.getPermit());
            each.put("acquireDuration", ImmutableMap.of("max", permitStats.getDuration().getMax() == null ? 0 : permitStats.getDuration().getMax(), "min", permitStats.getDuration().getMin() == null ? 0 : permitStats.getDuration().getMin(), "avg", permitStats.getDuration().getAvg()));
            each.put("permits", ImmutableMap.of("peakTimestamp", permitStats.getPeak().getCount() == null ? "" : permitStats.getPeak().getCount()  + "/" + permitStats.getPeak().getTimestamp() == null ? "" : permitStats.getPeak().getTimestamp(), "current", permitStats.getActives(), "successes", permitStats.getDuration().getTotal(), "fails", permitStats.getFailedPermits()));
            each.put("lastAcquireTimestamp", permitStats.getLastTimestamp());
            permitStatuses.add(each);
        }
        return permitStatuses;
    }


}
