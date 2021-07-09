package gg.codie.mineonline.plugin.bukkit;

import gg.codie.mineonline.protocol.MineOnlineURLStreamHandler;
import gg.codie.mineonline.protocol.MineOnlineURLStreamHandlerFactory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OnlineModeFixPlugin extends JavaPlugin {
    Logger log;

    public void enableOnlineModeFix() {
        Logger.getLogger("Minecraft").log(Level.INFO, "Enabling online-mode fix.");

        URL.setURLStreamHandlerFactory(new MineOnlineURLStreamHandlerFactory());
    }

    @Override
    public void onEnable() {
        initialize();
    }

    public void initialize() {
        enableOnlineModeFix();
    }

    @Override
    public void onDisable() {

    }
}
