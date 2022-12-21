package gg.codie.mineonline.protocol;

import gg.codie.mineonline.protocol.http.Handler;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class MineOnlineURLStreamHandlerFactory implements URLStreamHandlerFactory {
    private final Class<? extends URLConnection> defaultHttpConnectionClass;

    public MineOnlineURLStreamHandlerFactory() {
        try {
            URL foo = new URL("http://example.com");
            // Doesn't actually establish a connection
            defaultHttpConnectionClass = foo.openConnection().getClass();
        } catch (Exception e) {
            // this should never happen as the URL is hardcoded, shouldn't be invalid.
            throw new RuntimeException(e);
        }
    }


    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("http".equals(protocol)) {
            return new Handler(defaultHttpConnectionClass);
        }

        return null;
    }
}
