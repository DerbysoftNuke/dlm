package com.derbysoft.nuke.dlm.server;

import com.derbysoft.nuke.dlm.IPermit;
import com.derbysoft.nuke.dlm.IPermitManager;
import com.derbysoft.nuke.dlm.PermitBuilderManager;
import com.derbysoft.nuke.dlm.PermitSpec;
import com.derbysoft.nuke.dlm.server.repository.IPermitRepository;
import com.derbysoft.nuke.dlm.server.status.StatsCenter;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Created by passyt on 16-9-2.
 */
@Component
public class PermitManager implements IPermitManager {

    private Logger log = LoggerFactory.getLogger(PermitManager.class);

    private final IPermitRepository repository;

    @Autowired
    public PermitManager(IPermitRepository repository) {
        this.repository = repository;
        for (Map.Entry<String, IPermit> each : repository.getAll().entrySet()) {
            register(each.getKey(), each.getValue().name(), new PermitSpec(each.getValue().spec()));
        }
    }

    @Override
    public boolean register(String resourceId, String permitName, PermitSpec spec) {
        log.debug("Register permit {} with spec {} by id {}", permitName, spec, resourceId);

        IPermit permit = buildPermit(permitName, spec);
        StatsCenter.getInstance().register(resourceId, permit);

        if (repository.putIfAbsent(resourceId, permit) != null) {
            log.warn("An existing permit {} with id {}, not allow to register", repository.get(resourceId), resourceId);
            return false;
        }
        return true;
    }

    @Override
    public boolean unregister(String resourceId) {
        repository.remove(resourceId);
        return true;
    }

    @Override
    public boolean isExisting(String resourceId) {
        return repository.contains(resourceId);
    }

    @Override
    public IPermit getPermit(String resourceId) {
        IPermit permit = repository.get(resourceId);
        //TODO optimize performance
        return (IPermit) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{IPermit.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object result = method.invoke(permit, args);
                if ("acquire".equals(method.getName())) {
                    StatsCenter.getInstance().increasePermit(resourceId);
                } else if ("tryAcquire".equals(method.getName())) {
                    if (Boolean.TRUE.equals(result)) {
                        StatsCenter.getInstance().increasePermit(resourceId);
                    }
                } else if ("release".equals(method.getName())) {
                    StatsCenter.getInstance().decreasePermit(resourceId);
                }
                return result;
            }
        });

    }

    public Map<String, IPermit> permits() {
        return ImmutableMap.copyOf(repository.getAll());
    }

    protected IPermit buildPermit(String permitName, PermitSpec spec) {
        IPermit permit = PermitBuilderManager.getInstance().buildPermit(permitName, spec);
        if (permit == null) {
            throw new IllegalArgumentException("PermitSerializer not found by permit " + permitName + " with spec " + spec);
        }

        return permit;
    }

}