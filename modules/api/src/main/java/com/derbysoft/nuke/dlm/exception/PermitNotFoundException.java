package com.derbysoft.nuke.dlm.exception;

/**
 * Created by passyt on 16-9-4.
 */
public class PermitNotFoundException extends PermitException {

    private String resourceId;

    public PermitNotFoundException(String resourceId) {
        super("Permit not found by " + resourceId);
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return resourceId;
    }
}
