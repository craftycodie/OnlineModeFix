package gg.codie.mineonline.protocol.http;

import gg.codie.mineonline.protocol.CheckServerURLConnection;
import sun.net.www.protocol.http.HttpURLConnection;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {
    private final Class<? extends URLConnection> defaultHttpConnectionClass;

    public Handler() {
        // Necessary for vanilla servers which don't use the factory.
        defaultHttpConnectionClass = null;
    }

    public Handler(Class<? extends URLConnection> _defaultHttpConnectionClass) {
        defaultHttpConnectionClass = _defaultHttpConnectionClass;
    }
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        // Online-Mode fix
        if (url.toString().contains("/game/checkserver.jsp"))
            return new CheckServerURLConnection(url);
        else if (defaultHttpConnectionClass != null) {
            try {
                return defaultHttpConnectionClass.getConstructor(URL.class, Proxy.class).newInstance(url, null);
            } catch (Exception e) {
                // If the constructor isn't found, you can log that out. It's not expected.
                return null;
            }
        } else {
            try {
                Class sunHttpConnection = ClassLoader.getSystemClassLoader().loadClass("sun.net.www.protocol.http.HttpURLConnection");
                return (URLConnection) sunHttpConnection.getConstructor(URL.class, Proxy.class).newInstance(url, null);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
