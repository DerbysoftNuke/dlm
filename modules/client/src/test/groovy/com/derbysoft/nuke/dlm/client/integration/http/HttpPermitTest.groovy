package com.derbysoft.nuke.dlm.client.integration.http

import com.derbysoft.nuke.dlm.PermitSpec
import com.derbysoft.nuke.dlm.client.http.HttpPermitManager
import org.junit.Before
import org.junit.Test

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by passyt on 16-9-18.
 */
class HttpPermitTest {

    def HttpPermitManager manager;
    def resourceId = "http://www.baidu.com/";

    @Before
    public void init() {
        manager = new HttpPermitManager();
        manager.setServerUrl("http://127.0.0.1:8080");
    }

    @Test
    def void register() {
        println manager.register(resourceId, "SemaphorePermit", new PermitSpec("timeOut=3,total=5"));
    }

    @Test
    def void unregister() {
        println manager.unregister(resourceId);
    }

    @Test
    def void isExisting() {
        println manager.isExisting(resourceId);
    }

    @Test
    def void acquire() {
        manager.getPermit(resourceId).acquire();
    }

    @Test
    def void tryAcquire() {
        println manager.getPermit(resourceId).tryAcquire();
    }

    @Test
    def void tryAcquireTimeOut() {
        println manager.getPermit(resourceId).tryAcquire(100l, TimeUnit.MILLISECONDS);
    }

    @Test
    def void release() {
        manager.getPermit(resourceId).release();
    }

    @Test
    def void performance() {
        def tasks = [];
        def total = 1000;
        AtomicInteger a = new AtomicInteger(total);
        (1..total).each {
            tasks.add({
                def permit = null;
                try {
                    permit = manager.getPermit(resourceId);
                    println("acquiring...")
                    permit.acquire();
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    permit.release();
                    println("acquired, left " + a.decrementAndGet());
                }
            });
        }

        def pool = Executors.newFixedThreadPool(20);
        def start = System.currentTimeMillis();
        pool.invokeAll(tasks);
        def end = System.currentTimeMillis();
        println((end - start) + " ms: " + total * 1000f / (end - start) + " tps");
        pool.shutdown();
    }

}
