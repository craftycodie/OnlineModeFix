package gg.codie.mineonline.plugin.bukkit;

import gg.codie.minecraft.server.MinecraftColorCodeProvider;
import gg.codie.mineonline.discord.DiscordChatBridge;
import org.bukkit.event.player.*;

public class MineOnlineBroadcastListener extends PlayerListener {
    DiscordChatBridge discord;
    MinecraftColorCodeProvider colorCodeProvider = new MinecraftColorCodeProvider();

    public MineOnlineBroadcastListener(DiscordChatBridge discord) {
        this.discord = discord;
    }

    public void onPlayerQuit(PlayerQuitEvent event) {
        MineOnlineBroadcastPlugin.lastPing = 0;
        if (discord != null)
            discord.sendDiscordMessage("", "**" + colorCodeProvider.removeColorCodes(event.getPlayer().getName()) + "** left the game.");
    }

    public void onPlayerLogin(PlayerLoginEvent event) {
        MineOnlineBroadcastPlugin.lastPing = 0;
        if (discord != null)
            discord.sendDiscordMessage("", "**" + colorCodeProvider.removeColorCodes(event.getPlayer().getName()) + "** joined the game.");
    }

    public void onPlayerJoin(PlayerJoinEvent event) {
        MineOnlineBroadcastPlugin.lastPing = 0;
        if (discord != null)
            discord.sendDiscordMessage("", "**" + colorCodeProvider.removeColorCodes(event.getPlayer().getName()) + "** joined the game.");
    }

    public void onPlayerKick(PlayerKickEvent event) {
        MineOnlineBroadcastPlugin.lastPing = 0;
        if (discord != null)
            discord.sendDiscordMessage("", "**" + colorCodeProvider.removeColorCodes(event.getPlayer().getName()) + "** left the game.");
    }

    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        if (discord != null)
            discord.sendDiscordMessage(colorCodeProvider.removeColorCodes(event.getPlayer().getName()), colorCodeProvider.removeColorCodes(event.getMessage()));
    }
}
