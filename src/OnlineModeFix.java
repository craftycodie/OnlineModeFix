
import gg.codie.mineonline.plugin.ProxyThread;

import java.io.*;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.logging.Logger;

public class OnlineModeFix extends Plugin {
    private static String NAME = "OnlineModeFix";
    Logger log;
    ProxyThread proxyThread;
    boolean initialized;

    public void launchProxy() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        proxyThread = new ProxyThread(serverSocket);
        proxyThread.start();

        System.out.println("Enabling online-mode fix.");

        System.setProperty("http.proxyHost", serverSocket.getInetAddress().getHostAddress());
        System.setProperty("http.proxyPort", "" + serverSocket.getLocalPort());
        System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1");
    }

    public void stopProxy() {
        if (proxyThread != null) {
            proxyThread.stop();
            proxyThread = null;
        }
    }

    public void enable() {
        initialize();

        this.setName(NAME);
    }

    public void initialize() {
        if (initialized)
            return;

        this.log = Logger.getLogger("Minecraft");

        Properties propertiesFile = new Properties();

        try {
            propertiesFile.load(new FileInputStream(new File("server.properties")));
            boolean onlineMode = propertiesFile.getProperty("online-mode", "true").equals("true");

            if (onlineMode)
                launchProxy();
        } catch (Exception ex) {
            log.warning("Failed to enable online-mode fix. Authentication may fail.");
        }
        initialized = true;
    }

    private void register(PluginLoader.Hook hook, PluginListener.Priority priority) {

    }

    private void unregister() {

    }

    private void register(PluginLoader.Hook hook) {

    }

    public void disable() {
        if (!initialized)
            return;

        unregister();

        stopProxy();

        initialized = false;
    }
}
