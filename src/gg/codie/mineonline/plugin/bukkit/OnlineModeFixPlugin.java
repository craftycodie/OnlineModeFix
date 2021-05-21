package gg.codie.mineonline.plugin.bukkit;

import gg.codie.mineonline.plugin.ProxyThread;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.logging.Logger;

public class OnlineModeFixPlugin extends JavaPlugin {
    Thread broadcastThread;
    Logger log;
    ProxyThread proxyThread;

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

    @Override
    public void onEnable() {
        initialize();
    }

    public void initialize() {
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
    }

    @Override
    public void onDisable() {
        broadcastThread.interrupt();
        stopProxy();
    }
}
