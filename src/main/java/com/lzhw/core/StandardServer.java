package com.lzhw.core;

/**
 * Created by admin on 2017/5/5.
 */
public class StandardServer extends LifecycleBase {
    /**
     * The set of Services associated with this Server.
     */
    private Service services[] = new Service[0];
    private final Object servicesLock = new Object();

    @Override
    protected void initInternal() throws Exception {
        // Initialize our defined Services
        for (int i = 0; i < services.length; i++) {
            services[i].init();
        }
    }

    protected void startInternal()throws Exception {
        // Start our defined Services
        synchronized (servicesLock) {
            for (int i = 0; i < services.length; i++) {
                services[i].start();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new StandardServer().start();
        Thread.sleep(100000);
    }
}
