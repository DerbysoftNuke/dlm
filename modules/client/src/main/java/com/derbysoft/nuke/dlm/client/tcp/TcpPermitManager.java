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
public class TcpPermitManager extends AbstractTcpPermitClient implements IPermitManager {

    public TcpPermitManager(String host, int port) throws InterruptedException {
        super(host, port);
    }

    @Override
    public boolean register(String resourceId, String permitName, PermitSpec spec) {
        return execute(new RegisterRequest(resourceId, permitName, spec.getSpecification())).isSuccessful();
    }

    @Override
    public boolean unregister(String resourceId) {
        return execute(new UnRegisterRequest(resourceId)).isSuccessful();
    }

    @Override
    public boolean isExisting(String resourceId) {
        return execute(new ExistingRequest(resourceId)).isExisting();
    }

    @Override
    public IPermit getPermit(String resourceId) {
        return new TcpPermit(resourceId, channel, group, bootstrap);
    }
}
