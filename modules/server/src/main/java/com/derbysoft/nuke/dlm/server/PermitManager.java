package com.derbysoft.nuke.dlm.server;

import com.derbysoft.nuke.dlm.IPermit;
import com.derbysoft.nuke.dlm.IPermitManager;
import com.derbysoft.nuke.dlm.PermitBuilderManager;
import com.derbysoft.nuke.dlm.PermitSpec;
import com.derbysoft.nuke.dlm.server.status.DefaultStats;
import com.derbysoft.nuke.dlm.standalone.StandalonePermit;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by passyt on 16-9-2.
 */
@Component
public class PermitManager implements IPermitManager {

    private Logger log = LoggerFactory.getLogger(PermitManager.class);
    private static ConcurrentMap<String, StatPermit> permits = new ConcurrentHashMap<>();
    private static final String filePath = System.getProperty("java.io.tmpdir");
    private ConcurrentMap<String, IPermit> updatePermits = new ConcurrentHashMap<>();

    static {
        StandalonePermit.init();
        loadPermits();
    }

    private static void loadPermits() {
        try {
            ConcurrentMap<String, StatPermit> each = (ConcurrentMap<String, StatPermit>) new ObjectInputStream(new FileInputStream(createFile())).readObject();
            for (Map.Entry<String, StatPermit> entry : each.entrySet()) {
                permits.put(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static File createFile() {
        try {
            File file = new File(filePath + "permits.json");
            if (!file.exists()) {
                file.createNewFile();
                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                outputStream.writeObject(new ConcurrentHashMap<>());
                outputStream.close();
            }
            return file;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void saveOrUpdatePermists(ConcurrentMap<String, StatPermit> newPermits, String... resourceIds) {
        updatePermits.clear();
        try {
            File file = createFile();
            Map<String, StatPermit> each = (Map<String, StatPermit>) new ObjectInputStream(new FileInputStream(file)).readObject();
            if (resourceIds.length > 0) {
                for (String resourceId : resourceIds) {
                    each.remove(resourceId);
                }
            } else {
                for (Map.Entry<String, StatPermit> entry : newPermits.entrySet()) {
                    updatePermits.put(entry.getKey(), entry.getValue());
                }
            }
            for (Map.Entry<String, StatPermit> entry : each.entrySet()) {
                updatePermits.put(entry.getKey(), entry.getValue());
            }
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(updatePermits);
            outputStream.close();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean register(String resourceId, String permitName, PermitSpec spec) {
        log.debug("Register permit {} with spec {} by id {}", permitName, spec, resourceId);

        if (permits.putIfAbsent(resourceId, buildPermit(permitName, spec)) != null) {
            log.warn("An existing permit {} with id {}, not allow to register", permits.get(resourceId), resourceId);
            return false;
        }
        saveOrUpdatePermists(permits);
        return true;
    }

    @Override
    public boolean unregister(String resourceId) {
        permits.remove(resourceId);
        saveOrUpdatePermists(permits, resourceId);
        return true;
    }

    @Override
    public boolean isExisting(String resourceId) {
        return permits.containsKey(resourceId);
    }

    @Override
    public IPermit getPermit(String resourceId) {
        return permits.get(resourceId);
    }

    public Map<String, StatPermit> permits() {
        return ImmutableMap.copyOf(this.permits);
    }

    protected StatPermit buildPermit(String permitName, PermitSpec spec) {
        IPermit permit = PermitBuilderManager.getInstance().buildPermit(permitName, spec);
        if (permit == null) {
            throw new IllegalArgumentException("Permit not found by permit " + permitName + " with spec " + spec);
        }

        return new StatPermit(permit);
    }

    public static class StatPermit implements IPermit {

        private static final long serialVersionUID = -3222578541660680211L;

        private final IPermit permit;
        private final DefaultStats stats;

        public StatPermit(IPermit permit) {
            this.permit = permit;
            this.stats = new DefaultStats();
        }

        @Override
        public void acquire() {
            permit.acquire();
            stats.increment();
        }

        @Override
        public boolean tryAcquire() {
            if (permit.tryAcquire()) {
                stats.increment();
                return true;
            }

            return false;
        }

        @Override
        public boolean tryAcquire(long timeout, TimeUnit unit) {
            if (permit.tryAcquire(timeout, unit)) {
                stats.increment();
                return true;
            }

            return false;
        }

        @Override
        public void release() {
            permit.release();
            stats.decrement();
        }

        public DefaultStats getStats() {
            return stats;
        }

        public IPermit getPermit() {
            return permit;
        }
    }

}
