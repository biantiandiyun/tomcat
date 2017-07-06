package com.lzhw.connector.nio;

import com.lzhw.connector.AbstractEndpoint;
import com.lzhw.connector.AbstractProtocol;

/**
 * Created by admin on 2017/5/8.
 */
public class Http11NioProtocol extends AbstractProtocol {

    public Http11NioProtocol( ) {
        super(new NioEndpoint());
    }
}
