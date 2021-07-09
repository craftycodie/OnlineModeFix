import gg.codie.mineonline.protocol.MineOnlineURLStreamHandlerFactory;

import java.net.URL;
import java.util.logging.Logger;

public class OnlineModeFix extends Plugin {
    private static String NAME = "OnlineModeFix";

    static boolean enabled;

    public void enableOnlineMode() {
        if (enabled)
            return;
        else
            enabled = true;

        Logger.getLogger("Minecraft").info("Enabling online-mode fix.");

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
