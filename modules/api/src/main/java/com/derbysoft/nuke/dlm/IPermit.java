package com.derbysoft.nuke.dlm;

import java.util.concurrent.TimeUnit;

/**
 * @author Passyt
 */
public interface IPermit {

    /**
     * Acquires the permit.
     * <p>
     * <p>
     * If permit is not available then the current thread becomes disabled for
     * thread scheduling purposes and lies dormant until the permit has been
     * acquired.
     * </p>
     */
    void acquire();

    /**
     * Acquires the permit only if it is free at the time of invocation.
     * <p>
     * <p>
     * Acquires the permit if it is available and returns immediately with the
     * value {@code true}. If the permit is not available then this method will
     * return immediately with the value {@code false}.
     * <p>
     * <pre>
     *  {@code
     * IPermit permit = ...;
     * if (permit.tryAcquire()) {
     *   try {
     *     // manipulate protected state
     *   } finally {
     *     permit.release();
     *   }
     * } else {
     *   // perform alternative actions
     * }}
     * </pre>
     */
    boolean tryAcquire();

    /**
     * Acquires the permit if it is free within the given waiting time and the
     * current thread has not been {@linkplain Thread#interrupt interrupted}.
     *
     * @param timeout
     * @param unit
     */
    boolean tryAcquire(long timeout, TimeUnit unit);

    /**
     * release permit
     */
    void release();

    /**
     * @return
     */
    //TODO code refactor
    default String name() {
        return getClass().getSimpleName();
    }

    /**
     * @return
     */
    //TODO code refactor
    default String spec() {
        return toString();
    }

}
