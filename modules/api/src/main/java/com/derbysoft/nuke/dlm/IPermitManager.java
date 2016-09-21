package com.derbysoft.nuke.dlm;

/**
 * permit manager in server side to manage all permit settings
 */
public interface IPermitManager {

    /**
     * register a new permit in register server, return error if an existing permit
     *
     * @param resourceId the id of resource in register server
     * @param permitName permit name(class name or alias)
     * @param spec       permit spec
     * @return
     */
    boolean register(String resourceId, String permitName, PermitSpec spec);

    /**
     * @param resourceId the id of resource in register server
     * @return
     */
    boolean unregister(String resourceId);

    /**
     * @param resourceId
     * @return
     */
    boolean isExisting(String resourceId);

    /**
     * @param resourceKey the id of resource in register server
     * @return
     */
    //TODO support read-write permit, resource permit etc
    IPermit getPermit(String resourceKey);

}
