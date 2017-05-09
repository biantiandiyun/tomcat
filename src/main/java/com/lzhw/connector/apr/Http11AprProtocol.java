package com.lzhw.connector.apr;

import java.util.concurrent.Executor;

/**
 * Created by admin on 2017/5/8.
 */
public class Http11AprProtocol extends AbstractHttp11Protocol<Long> {
    public Http11AprProtocol() {
        super(new AprEndpoint());
    }
}
