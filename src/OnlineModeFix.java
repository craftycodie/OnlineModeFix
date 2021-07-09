import gg.codie.mineonline.protocol.MineOnlineURLStreamHandlerFactory;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

public class OnlineModeFix extends Plugin {
    private static String NAME = "OnlineModeFix";
    Logger log;

    public void enableOnlineMode() {
        this.log = Logger.getLogger("Minecraft");

        log.info("Enabling online-mode fix.");

        URL.setURLStreamHandlerFactory(new MineOnlineURLStreamHandlerFactory());
    }

    public void enable() {
        initialize();

        this.setName(NAME);
    }

    public void initialize() {
        enableOnlineMode();
    }

    private void register(PluginLoader.Hook hook, PluginListener.Priority priority) {

    }

    private void unregister() {

    }

    private void register(PluginLoader.Hook hook) {

    }

    public void disable() {

    }
}
