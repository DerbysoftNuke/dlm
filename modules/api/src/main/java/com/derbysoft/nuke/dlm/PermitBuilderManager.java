package com.derbysoft.nuke.dlm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by passyt on 16-9-3.
 */
public class PermitBuilderManager {

    private static final Logger log = LoggerFactory.getLogger(PermitBuilderManager.class);
    private final ConcurrentMap<String, IPermitBuilder> builders = new ConcurrentHashMap<>();
    private static final PermitBuilderManager INSTANCE = new PermitBuilderManager();

    private PermitBuilderManager() {
    }

    public static PermitBuilderManager getInstance() {
        return INSTANCE;
    }

    public void registerPermitBuilder(IPermitBuilder builder, String... permitNames) {
        for (String permitName : permitNames) {
            if (builders.putIfAbsent(permitName, builder) != null) {
                log.warn("Existing permit builder by name {} and ignore to register", permitName);
                return;
            }
        }
    }

    public IPermit buildPermit(String permitName, PermitSpec spec) {
        IPermitBuilder builder = builders.get(permitName);
        if (builder == null) {
            return null;
        }

        return builder.build(spec);
    }

}
