import gg.codie.minecraft.server.MinecraftColorCodeProvider;
import gg.codie.mineonline.discord.DiscordChatBridge;

public class MineOnlineBroadcastListener extends PluginListener {
    DiscordChatBridge discord;
    MinecraftColorCodeProvider colorCodeProvider = new MinecraftColorCodeProvider();

    public MineOnlineBroadcastListener(DiscordChatBridge discord) {
        this.discord = discord;
    }

    public void onKick(Player mod, Player player, String reason) {
        MineOnlineBroadcast.lastPing = 0;
        if (discord != null)
            discord.sendDiscordMessage("", "**" + colorCodeProvider.removeColorCodes(player.getName()) + "** left the game.");
    }

    public void onDisconnect(Player player) {
        MineOnlineBroadcast.lastPing = 0;
        if (discord != null)
            discord.sendDiscordMessage("", "**" + colorCodeProvider.removeColorCodes(player.getName()) + "** left the game.");
    }

    public void onLogin(Player player) {
        MineOnlineBroadcast.lastPing = 0;
        if (discord != null)
            discord.sendDiscordMessage("", "**" + colorCodeProvider.removeColorCodes(player.getName()) + "** joined the game.");
    }

    @Override
    public boolean onChat(Player player, String message) {
        if (discord != null)
            discord.sendDiscordMessage(colorCodeProvider.removeColorCodes(player.getName()), colorCodeProvider.removeColorCodes(message));
        return super.onChat(player, message);
    }
}
