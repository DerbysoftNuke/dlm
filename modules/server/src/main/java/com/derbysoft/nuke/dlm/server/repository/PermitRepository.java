package com.derbysoft.nuke.dlm.server.repository;

import com.derbysoft.nuke.dlm.IPermit;
import com.derbysoft.nuke.dlm.standalone.StandalonePermit;
import com.google.common.collect.ImmutableMap;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by passyt on 16-9-21.
 */
@Repository
public class PermitRepository implements IPermitRepository {

    private final DB db;
    private final ConcurrentMap<String, IPermit> permits;

    static {
        StandalonePermit.init();
    }

    public PermitRepository(DB db) {
        this.db = db;
        this.permits = (ConcurrentMap<String, IPermit>) db.hashMap("permits").keySerializer(Serializer.STRING).valueSerializer(new PermitSerializer()).createOrOpen();
    }

    @Override
    public IPermit putIfAbsent(String resourceId, IPermit permit) {
        try {
            return permits.putIfAbsent(resourceId, permit);
        } finally {
            db.commit();
        }
    }

    @Override
    public IPermit put(String resourceId, IPermit permit) {
        try {
            return permits.put(resourceId, permit);
        } finally {
            db.commit();
        }
    }

    @Override
    public void remove(String resourceId) {
        try {
            permits.remove(resourceId);
        } finally {
            db.commit();
        }
    }

    @Override
    public boolean contains(String resourceId) {
        return permits.containsKey(resourceId);
    }

    @Override
    public IPermit get(String resourceId) {
        return permits.get(resourceId);
    }

    @Override
    public Map<String, IPermit> getAll() {
        return ImmutableMap.copyOf(permits);
    }

}