package com.lzhw.core;

/**
 * Created by admin on 2017/5/5.
 */
public class StandardServer {
    /**
     * The set of Services associated with this Server.
     */
    private Service services[] = new Service[0];
    private final Object servicesLock = new Object();

    protected void startInternal(){
        // Start our defined Services
        synchronized (servicesLock) {
            for (int i = 0; i < services.length; i++) {
                services[i].start();
            }
        }
    }
}
