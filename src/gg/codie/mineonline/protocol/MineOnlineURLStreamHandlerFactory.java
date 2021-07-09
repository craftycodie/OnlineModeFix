package gg.codie.mineonline.protocol;

import gg.codie.mineonline.protocol.http.Handler;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class MineOnlineURLStreamHandlerFactory implements URLStreamHandlerFactory {
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("http".equals(protocol)) {
            return new Handler();
        }

        return null;
    }
}
