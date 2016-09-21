package com.derbysoft.nuke.dlm.server.repository;

import com.derbysoft.nuke.dlm.IPermit;
import com.derbysoft.nuke.dlm.standalone.StandalonePermit;
import com.derbysoft.nuke.dlm.standalone.TokenBucketPermit;
import com.google.common.collect.ImmutableMap;
import org.mapdb.*;
import org.mapdb.serializer.GroupSerializer;
import org.mapdb.serializer.SerializerString;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
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
            return permits.putIfAbsent(resourceId, permit);
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

    public static void main(String... a) {
        DB db = DBMaker.fileDB("/tmp/test.db")
                .fileChannelEnable()
                .fileMmapEnableIfSupported()
                .fileMmapPreclearDisable()
                .transactionEnable()
                .closeOnJvmShutdown()
                .make();
        HTreeMap<String, IPermit> map = db.hashMap("test", Serializer.STRING, new PermitSerializer()).createOrOpen();
        System.out.println(map.get("1"));
        map.putIfAbsent("1", new TokenBucketPermit(2d));
        System.out.println(map.get("1"));
        db.commit();
    }

}
