package com.derbysoft.nuke.dlm.client.integration.tcp

import com.derbysoft.nuke.dlm.PermitSpec
import com.derbysoft.nuke.dlm.client.tcp.TcpPermitManager
import org.junit.Before
import org.junit.Test

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
        println manager.register(resourceId, "SemaphorePermit", new PermitSpec("total=1"))
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
        def total = 20000;
        AtomicInteger a = new AtomicInteger(total);
        (1..total).each {
            tasks.add({
                def permit = null;
                try {
                    permit = manager.getPermit(resourceId);
                    println("acquiring...");
                    permit.acquire();
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    permit.release();
                    println("acquired, left " + a.decrementAndGet());
                }
            });
        }

        def start = System.currentTimeMillis();
        def pool = Executors.newFixedThreadPool(20);
        pool.invokeAll(tasks);
        pool.shutdown();
        def end = System.currentTimeMillis();
        println((end - start) + " ms: " + total * 1000f / (end - start) + " tps");
    }

}
