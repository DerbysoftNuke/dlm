package com.derbysoft.nuke.dlm.client.tcp;

import com.derbysoft.nuke.dlm.IPermit;
import com.derbysoft.nuke.dlm.IPermitManager;
import com.derbysoft.nuke.dlm.PermitSpec;
import com.derbysoft.nuke.dlm.model.ExistingRequest;
import com.derbysoft.nuke.dlm.model.RegisterRequest;
import com.derbysoft.nuke.dlm.model.UnRegisterRequest;

/**
 * Created by DT219 on 2016-09-14.
 */
public class TcpPermitManager implements IPermitManager {

    private final TcpPermitClient client;

    public TcpPermitManager(String host, int port) throws InterruptedException {
        this.client = new TcpPermitClient(host, port);
    }

    @Override
    public boolean register(String resourceId, String permitName, PermitSpec spec) {
        return client.execute(new RegisterRequest(resourceId, permitName, spec.getSpecification())).isSuccessful();
    }

    @Override
    public boolean unregister(String resourceId) {
        return client.execute(new UnRegisterRequest(resourceId)).isSuccessful();
    }

    @Override
    public boolean isExisting(String resourceId) {
        return client.execute(new ExistingRequest(resourceId)).isExisting();
    }

    @Override
    public IPermit getPermit(String resourceId) {
        return new TcpPermit(resourceId, client);
    }
}
