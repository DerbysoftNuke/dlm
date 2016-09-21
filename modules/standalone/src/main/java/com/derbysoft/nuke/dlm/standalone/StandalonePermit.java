package com.derbysoft.nuke.dlm.standalone;

import com.derbysoft.nuke.dlm.IPermit;
import com.derbysoft.nuke.dlm.PermitBuilderManager;

/**
 * @author Passyt
 */
public abstract class StandalonePermit implements IPermit {

    private static final long serialVersionUID = -3222578661660680811L;
    public static void init() {
        PermitBuilderManager.getInstance().registerPermitBuilder((spec) -> {
            return new LeakyBucketPermit(spec.required(true).doubleValueOf("permitsPerSecond"));
        }, LeakyBucketPermit.class.getSimpleName(), LeakyBucketPermit.class.getName());

        PermitBuilderManager.getInstance().registerPermitBuilder((spec) -> {
            return new ReentrantPermit();
        }, ReentrantPermit.class.getSimpleName(), ReentrantPermit.class.getName());

        PermitBuilderManager.getInstance().registerPermitBuilder((spec) -> {
            return new SemaphorePermit(spec.required(true).intValueOf("total"));
        }, SemaphorePermit.class.getSimpleName(), SemaphorePermit.class.getName());

        PermitBuilderManager.getInstance().registerPermitBuilder((spec) -> {
            return new TokenBucketPermit(spec.required(true).doubleValueOf("permitsPerSecond"));
        }, TokenBucketPermit.class.getSimpleName(), TokenBucketPermit.class.getName());
    }

}
