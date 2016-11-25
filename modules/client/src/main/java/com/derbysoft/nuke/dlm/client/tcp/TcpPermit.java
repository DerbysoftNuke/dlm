package com.derbysoft.nuke.dlm.client.tcp;

import com.derbysoft.nuke.dlm.IPermit;
import com.derbysoft.nuke.dlm.model.ReleaseRequest;
import com.derbysoft.nuke.dlm.model.TryAcquireRequest;

import java.util.concurrent.TimeUnit;

/**
 * Created by DT219 on 2016-09-14.
 */
public class TcpPermit implements IPermit {

    private final TcpPermitClient client;
    private String resourceId;

    protected TcpPermit(String resourceId, TcpPermitClient client) {
        this.resourceId = resourceId;
        this.client = client;
    }

    @Override
    public void acquire() {
        while (!tryAcquire(TcpPermitClient.DEFAULT_TIMEOUT_SECOND, TimeUnit.SECONDS)) {
        }
    }

    @Override
    public boolean tryAcquire() {
        return client.execute(new TryAcquireRequest(resourceId)).isSuccessful();
    }

    @Override
    public boolean tryAcquire(long timeout, TimeUnit unit) {
        return client.execute(new TryAcquireRequest(resourceId, timeout, unit)).isSuccessful();
    }

    @Override
    public void release() {
        client.execute(new ReleaseRequest(resourceId));
    }
}
