package com.derbysoft.nuke.dlm.server.repository;

import com.derbysoft.nuke.dlm.IPermit;
import com.derbysoft.nuke.dlm.standalone.StandalonePermit;
import com.google.common.collect.ImmutableMap;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by passyt on 16-9-21.
 */
@Repository
public class PermitRepository implements IPermitRepository {

    private final DB db;
    private final ConcurrentMap<String, IPermit> cache = new ConcurrentHashMap<>();
    private final HTreeMap<String, IPermit> target;

    static {
        StandalonePermit.init();
    }

    public PermitRepository(DB db) {
        this.db = db;
        target = db.hashMap("permits").valueInline().keySerializer(Serializer.STRING).valueSerializer(new PermitSerializer()).createOrOpen();
        cache.putAll(target);
    }

    @Override
    public IPermit putIfAbsent(String resourceId, IPermit permit) {
        try {
            target.putIfAbsent(resourceId, permit);
        } finally {
            db.commit();
        }
        return cache.putIfAbsent(resourceId, permit);
    }

    @Override
    public IPermit put(String resourceId, IPermit permit) {
        try {
            target.put(resourceId, permit);
        } finally {
            db.commit();
        }
        cache.put(resourceId, permit);
        return permit;
    }

    @Override
    public void remove(String resourceId) {
        try {
            target.remove(resourceId);
        } finally {
            db.commit();
        }
        cache.remove(resourceId);
    }

    @Override
    public boolean contains(String resourceId) {
        return cache.containsKey(resourceId);
    }

    @Override
    public IPermit get(String resourceId) {
        return cache.get(resourceId);
    }

    @Override
    public Map<String, IPermit> getAll() {
        return ImmutableMap.copyOf(cache);
    }

}
