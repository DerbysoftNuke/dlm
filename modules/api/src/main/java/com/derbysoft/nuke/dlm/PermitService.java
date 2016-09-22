package com.derbysoft.nuke.dlm;

import com.derbysoft.nuke.dlm.model.IPermitRequest;
import com.derbysoft.nuke.dlm.model.IPermitResponse;

/**
 * Created by passyt on 16-9-4.
 */
public class PermitService<RS extends IPermitResponse, RQ extends IPermitRequest<RS>> implements IPermitService<RS, RQ> {

    private final IPermitManager permitManager;

    public PermitService(IPermitManager permitManager) {
        this.permitManager = permitManager;
    }

    @Override
    public RS execute(RQ request) {
        return request.executeBy(permitManager);
    }

}
