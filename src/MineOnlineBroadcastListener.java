import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.logging.Logger;

public class MineOnlineBroadcastListener extends PlayerListener {
    private static ArrayList<String> PlayerList = new ArrayList();
    boolean _DEBUG = false;
    private Logger log;
    private MineOnlineBroadcast plugin = null;

    public MineOnlineBroadcastListener(MineOnlineBroadcast parent) {
        this.plugin = parent;
        this.log = Logger.getLogger("Minecraft");
    }

    public void onPlayerQuit(PlayerQuitEvent event) {
        MineOnlineBroadcast.lastPing = System.currentTimeMillis() - 40000;
    }

    public void onPlayerLogin(PlayerLoginEvent event) {
        MineOnlineBroadcast.lastPing = System.currentTimeMillis() - 40000;
    }

    public void onPlayerKick(PlayerKickEvent event) {
        MineOnlineBroadcast.lastPing = System.currentTimeMillis() - 40000;
    }
}
