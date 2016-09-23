package com.derbysoft.nuke.dlm.server.status;

import com.derbysoft.nuke.dlm.IPermit;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import io.netty.channel.Channel;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by passyt on 16-9-19.
 */
public class StatsCenter {

    private static final StatsCenter INSTANCE = new StatsCenter();

    private final TrafficStats tcpStats = new TrafficStats();
    private final TrafficStats httpStats = new TrafficStats();
    private final ConcurrentMap<String, PermitStats> permitStats = new ConcurrentHashMap<>();
    //TODO error radio
    //TODO traffic & permit

    private StatsCenter() {
    }

    public static StatsCenter getInstance() {
        return INSTANCE;
    }

    /**
     * @return http traffic stats
     */
    public TrafficStats getHttpStats() {
        return httpStats;
    }

    /**
     * @return tcp traffic stats
     */
    public TrafficStats getTcpStats() {
        return tcpStats;
    }

    public Stats register(String resourceId, IPermit permit) {
        PermitStats permitStats = this.permitStats.putIfAbsent(resourceId, new PermitStats(permit));
        return this.permitStats.get(resourceId);
    }

    public Stats update(String resourceId, IPermit permit) {
        PermitStats permitStats = this.permitStats.putIfAbsent(resourceId, new PermitStats(permit));
        if (permitStats != null) {
            permitStats.setPermit(permit);
        }
        return this.permitStats.get(resourceId);
    }

    /**
     * @return unmodifiable permit stats
     */
    public Map<String, PermitStats> permitStats() {
        return Collections.unmodifiableMap(permitStats);
    }

    /**
     * @param resourceId
     * @return
     */
    public StatsCenter increasePermit(String resourceId, long duration) {
        PermitStats permitStats = this.permitStats.get(resourceId);
        if (permitStats == null) {
            return this;
        }

        permitStats.increase();
        permitStats.getDuration().record(duration);
        return this;
    }

    /**
     * @param resourceId
     * @return
     */
    public StatsCenter decreasePermit(String resourceId) {
        PermitStats permitStats = this.permitStats.get(resourceId);
        if (permitStats == null) {
            return this;
        }

        permitStats.decrease();
        return this;
    }

    public StatsCenter increaseFailPermit(String resourceId) {
        PermitStats permitStats = this.permitStats.get(resourceId);
        if (permitStats == null) {
            return this;
        }

        permitStats.increaseFailedPermits();
        return this;
    }

    /**
     * traffic stats
     */
    public static class TrafficStats {

        private final Stats traffic = new Stats();
        //TODO wrapper channel to protect application
        private final Set<Channel> channels = new CopyOnWriteArraySet<>();

        public Stats getTraffic() {
            return traffic;
        }

        public Set<Channel> getChannels() {
            return Collections.unmodifiableSet(channels);
        }

        public TrafficStats active(Channel channel) {
            traffic.increase();
            channels.add(channel);
            return this;
        }

        public TrafficStats inactive(Channel channel) {
            traffic.decrease();
            channels.remove(channel);
            return this;
        }

    }

    /**
     * Permit Stats
     */
    public static class PermitStats extends Stats {

        private IPermit permit;
        private final Duration duration = new Duration();
        private final AtomicLong failedPermits = new AtomicLong(0);

        public PermitStats(IPermit permit) {
            this.permit = permit;
        }

        public IPermit getPermit() {
            return permit;
        }

        public void setPermit(IPermit permit) {
            this.permit = permit;
        }

        public Duration getDuration() {
            return duration;
        }

        public AtomicLong getFailedPermits() {
            return failedPermits;
        }

        public PermitStats increaseFailedPermits() {
            failedPermits.incrementAndGet();
            return this;
        }
    }

    public static class Duration {

        private Long max;
        private Long min;
        private long avg = 0L;
        private AtomicLong total = new AtomicLong(0);

        public Duration record(long duration) {
            if (max == null || duration > max) {
                max = duration;
            }
            if (min == null || duration < min) {
                min = duration;
            }
            long t = total.incrementAndGet();
            avg = (avg * (t - 1) + duration) / t;
            return this;
        }

        public Long getMax() {
            return max;
        }

        public Long getMin() {
            return min;
        }

        public long getAvg() {
            return avg;
        }

        public long getTotal() {
            return total.get();
        }
    }

    /**
     * Stats
     */
    public static class Stats {

        private Peak peak = new Peak();
        private AtomicLong actives = new AtomicLong(0);
        private ZonedDateTime lastTimestamp;

        public Stats increase() {
            lastTimestamp = ZonedDateTime.now();
            long total = actives.incrementAndGet();
            if (total >= peak.getCount().get()) {
                peak.getCount().set(total);
                peak.timestamp.set(ZonedDateTime.now());
            }
            return this;
        }

        public Stats decrease() {
            actives.decrementAndGet();
            return this;
        }

        public Peak getPeak() {
            return peak;
        }

        public AtomicLong getActives() {
            return actives;
        }

        public ZonedDateTime getLastTimestamp() {
            return lastTimestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Stats)) return false;
            Stats stats = (Stats) o;
            return Objects.equal(peak, stats.peak) &&
                    Objects.equal(actives, stats.actives) &&
                    Objects.equal(lastTimestamp, stats.lastTimestamp);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(peak, actives, lastTimestamp);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("peak", peak)
                    .add("actives", actives)
                    .add("lastTimestamp", lastTimestamp)
                    .toString();
        }

    }

    /**
     * peak
     */
    public static class Peak {

        private final AtomicLong count;
        private final AtomicReference<ZonedDateTime> timestamp;

        public Peak() {
            count = new AtomicLong(0);
            timestamp = new AtomicReference(null);
        }

        public AtomicLong getCount() {
            return count;
        }

        public AtomicReference<ZonedDateTime> getTimestamp() {
            return timestamp;
        }

        public Peak increment() {
            count.incrementAndGet();
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Peak)) return false;
            Peak peak = (Peak) o;
            return Objects.equal(count, peak.count) &&
                    Objects.equal(timestamp, peak.timestamp);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(count, timestamp);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("count", count)
                    .add("timestamp", timestamp)
                    .toString();
        }
    }
}
