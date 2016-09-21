package com.derbysoft.nuke.dlm.server.repository;

import com.derbysoft.nuke.dlm.IPermit;

import java.util.Map;

/**
 * Created by passyt on 16-9-21.
 */
public interface IPermitRepository {

    /**
     * @param resourceId
     * @param permit
     * @return
     */
    IPermit putIfAbsent(String resourceId, IPermit permit);

    /**
     * @param resourceId
     * @param permit
     * @return
     */
    IPermit put(String resourceId, IPermit permit);

    /**
     * @param resourceId
     */
    void remove(String resourceId);

    /**
     * @param resourceId
     * @return
     */
    boolean contains(String resourceId);

    /**
     * @param resourceId
     * @return
     */
    IPermit get(String resourceId);

    /**
     * @return
     */
    Map<String, IPermit> getAll();

}
