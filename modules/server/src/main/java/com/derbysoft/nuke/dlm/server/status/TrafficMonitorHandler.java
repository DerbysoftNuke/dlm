package com.derbysoft.nuke.dlm.server.status;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by passyt on 16-9-19.
 */
public class TrafficMonitorHandler extends ChannelHandlerAdapter {

    private final StatsCenter.TrafficStats trafficStats;

    public TrafficMonitorHandler(StatsCenter.TrafficStats trafficStats) {
        this.trafficStats = trafficStats;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        trafficStats.getTraffic().increase();
        trafficStats.active(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        trafficStats.getTraffic().decrease();
        trafficStats.inactive(ctx.channel());
    }

}