package com.derbysoft.nuke.dlm.server.status;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by passyt on 16-9-19.
 */
public class DefaultStats implements Serializable{

    private Peak peak = new Peak();
    private AtomicLong actives = new AtomicLong(0);
    private ZonedDateTime lastTimestamp;

    public DefaultStats increment() {
        lastTimestamp = ZonedDateTime.now();
        long total = actives.incrementAndGet();
        if (total >= peak.getCount().get()) {
            peak.getCount().set(total);
            peak.timestamp.set(ZonedDateTime.now());
        }
        return this;
    }

    public DefaultStats decrement() {
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
        if (!(o instanceof DefaultStats)) return false;
        DefaultStats that = (DefaultStats) o;
        return Objects.equal(peak, that.peak) &&
                Objects.equal(actives, that.actives) &&
                Objects.equal(lastTimestamp, that.lastTimestamp);
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

    public static class Peak implements Serializable{

        private static final long serialVersionUID = -3222378661660680211L;
        private final AtomicLong count;
        private final AtomicReference<ZonedDateTime> timestamp;

        public Peak() {
            count = new AtomicLong(0);
            timestamp = new AtomicReference(ZonedDateTime.now());
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
