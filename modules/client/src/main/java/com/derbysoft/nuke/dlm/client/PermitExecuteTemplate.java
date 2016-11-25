package com.derbysoft.nuke.dlm.client;

import com.derbysoft.nuke.dlm.IPermit;
import com.derbysoft.nuke.dlm.IPermitManager;
import com.derbysoft.nuke.dlm.exception.PermitNotFoundException;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by passyt on 2016/11/25.
 */
public class PermitExecuteTemplate {

    private final IPermitManager manager;

    public PermitExecuteTemplate(IPermitManager manager) {
        this.manager = manager;
    }

    /**
     * This method will be blocking is no permit granted until one can be granted
     *
     * @param resourceKey
     * @param callable
     * @param <T>
     * @return
     */
    public <T> T executeWith(String resourceKey, Callable<T> callable) {
        IPermit permit = getRequiredPermit(resourceKey);
        try {
            permit.acquire();
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            permit.release();
        }
    }

    /**
     * @param resourceKey
     * @param timeout
     * @param unit
     * @param callable
     * @param <T>
     * @return null if no permit granted within the given timeout
     */
    public <T> T executeWithTry(String resourceKey, long timeout, TimeUnit unit, Callable<T> callable) {
        IPermit permit = getRequiredPermit(resourceKey);
        try {
            if (permit.tryAcquire(timeout, unit)) {
                return callable.call();
            } else {
                return null;
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            permit.release();
        }
    }

    /**
     * @param resourceKey
     * @param callable
     * @param <T>
     * @return null if no permit granted immediately
     */
    public <T> T executeWithTry(String resourceKey, Callable<T> callable) {
        IPermit permit = getRequiredPermit(resourceKey);
        try {
            if (permit.tryAcquire()) {
                return callable.call();
            } else {
                return null;
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            permit.release();
        }
    }

    protected IPermit getRequiredPermit(String resourceKey) {
        IPermit permit = manager.getPermit(resourceKey);
        if (permit == null) {
            throw new PermitNotFoundException(resourceKey);
        }

        return permit;
    }

}
