import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

public class MineOnlineBroadcastListener extends PlayerListener {
    public MineOnlineBroadcastListener() {

    }

    public void onPlayerQuit(PlayerEvent event) {
        MineOnlineBroadcast.lastPing = System.currentTimeMillis() - 40000;
    }

    public void onPlayerLogin(PlayerLoginEvent event) {
        MineOnlineBroadcast.lastPing = System.currentTimeMillis() - 40000;
    }

    public void onPlayerKick(PlayerKickEvent event) {
        MineOnlineBroadcast.lastPing = System.currentTimeMillis() - 40000;
    }
}
