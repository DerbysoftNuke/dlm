package com.derbysoft.nuke.dlm.client.integration.tcp

import com.derbysoft.nuke.dlm.IPermit
import com.derbysoft.nuke.dlm.PermitSpec
import com.derbysoft.nuke.dlm.client.tcp.TcpPermitManager
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by passyt on 16-9-18.
 */
class TcpPermitTest {

    def TcpPermitManager manager;
    def resourceId = "123";

    @Before
    def void startup() {
        manager = new TcpPermitManager("127.0.0.1", 8081);
    }

//    @After
    def void shutdown() {
        manager.shutdown();
    }

    @Test
    def void register() {
        println manager.register(resourceId, "LeakyBucketPermit", new PermitSpec("permitsPerSecond=1000"))
        println manager.register(resourceId, "LeakyBucketPermit", new PermitSpec("permitsPerSecond=100"))
        println manager.register(resourceId, "LeakyBucketPermit", new PermitSpec("permitsPerSecond=100"))
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
    def void tryAcquireWithTimeout() {
        println manager.getPermit(resourceId).tryAcquire(100l, TimeUnit.MILLISECONDS);
    }

    @Test
    def void release() {
        manager.getPermit(resourceId).release();
    }

    @Test
    def void performance() {
        def tasks = [];
        def total = 10000;
        AtomicInteger a = new AtomicInteger(total);
        (1..total).each {
            tasks.add({
                def permit = manager.getPermit(resourceId)
                try {
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
