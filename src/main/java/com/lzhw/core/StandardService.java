package com.lzhw.core;

import com.lzhw.connector.Connector;

/**
 * Created by admin on 2017/5/5.
 */
public class StandardService implements Service {
    /**
     * The set of Connectors associated with this Service.
     */
    protected Connector connectors[] = new Connector[0];
    private final Object connectorsLock = new Object();

    @Override
    public void start() {
        synchronized (connectorsLock) {
            for (Connector connector: connectors) {
                // If it has already failed, don't try and start it
                connector.start();
            }
        }
    }
}
