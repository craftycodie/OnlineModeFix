package gg.codie.mineonline.plugin.bukkit;

import gg.codie.common.input.EColorCodeColor;
import gg.codie.minecraft.server.MinecraftColorCodeProvider;
import gg.codie.mineonline.api.MineOnlineAPI;
import gg.codie.mineonline.discord.DiscordChatBridge;
import gg.codie.mineonline.discord.IMessageRecievedListener;
import gg.codie.mineonline.discord.IShutdownListener;
import gg.codie.mineonline.discord.MinotarAvatarProvider;
import gg.codie.mineonline.plugin.ProxyThread;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MineOnlineBroadcastPlugin extends JavaPlugin {
    Thread broadcastThread;
    public static long lastPing;
    MineOnlineBroadcastListener listener;
    Logger log;
    ProxyThread proxyThread;
    DiscordChatBridge discord;
    private MineOnlineConfig mineOnlineConfig;
    String serverUUID;

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

    public static byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    @Override
    public void onEnable() {
        initialize();

        this.log = Logger.getLogger("Minecraft");

        this.log.info("Enabled MineOnlineBroadcast");

        broadcastThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (System.currentTimeMillis() - MineOnlineBroadcastPlugin.lastPing > 45000) {
                        lastPing = System.currentTimeMillis();
                        try {
                            Properties propertiesFile = new Properties();
                            propertiesFile.load(new FileInputStream(new File("server.properties")));

                            boolean isPublic = mineOnlineConfig.getConfigBoolean("public");
                            if (!isPublic)
                                return;

                            String ip = mineOnlineConfig.getConfigString("serverlist-ip");
                            if(ip == null) {
                                ip = propertiesFile.getProperty("server-ip", propertiesFile.getProperty("ip", null));
                            }
                            String port = mineOnlineConfig.getConfigString("serverlist-port");
                            if(port == null) {
                                propertiesFile.getProperty("server-port", propertiesFile.getProperty("port", "25565"));
                            }
                            int users = getServer().getOnlinePlayers().length;
                            int maxUsers = Integer.parseInt(propertiesFile.getProperty("max-players", "20"));
                            String name = mineOnlineConfig.getConfigString("server-name");
                            boolean onlineMode = propertiesFile.getProperty("online-mode", "true").equals("true");
                            String md5 = mineOnlineConfig.getConfigString("version-md5");
                            boolean whitelisted = propertiesFile.getProperty("whitelist", "false").equals("true");
                            boolean dontListPlayers = mineOnlineConfig.getConfigBoolean("dont-list-players");
                            String motd = mineOnlineConfig.getConfigString("serverlist-motd");
                            boolean useBetaEvolutionsAuth = mineOnlineConfig.getConfigBoolean("beta-evolutions-support");

                            String[] playerNames = Arrays.stream(getServer().getOnlinePlayers()).map(Player::getName).collect(Collectors.toList()).toArray(new String[users]);

                            String serverIcon = null;

                            try {
                                BufferedImage serverIconBufferedImage = ImageIO.read(new File(System.getProperty("user.dir") + File.separator + "server-icon.png"));
                                if (serverIconBufferedImage.getHeight() <= 64 && serverIconBufferedImage.getWidth() <= 64) {
                                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                    try {
                                        ImageIO.write(serverIconBufferedImage, "png", bos);
                                        byte[] bytes = bos.toByteArray();
                                        Base64.Encoder encoder = Base64.getEncoder();
                                        serverIcon = encoder.encodeToString(bytes);
                                        serverIcon = serverIcon.replace(System.lineSeparator(), "");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (IIOException ex) {
                                ex.printStackTrace();
                            }

                            serverUUID = MineOnlineAPI.listServer(
                                    ip,
                                    port,
                                    users,
                                    maxUsers,
                                    name,
                                    onlineMode,
                                    md5,
                                    whitelisted,
                                    playerNames,
                                    motd,
                                    dontListPlayers,
                                    useBetaEvolutionsAuth,
                                    serverIcon
                            );
                        } catch (IOException ex) {
                            // ignore.
                        }
                    }
                }
            }
        });

        broadcastThread.start();
    }

    public void initialize() {
        this.log = Logger.getLogger("Minecraft");

        MinecraftColorCodeProvider colorCodeProvider = new MinecraftColorCodeProvider();

        mineOnlineConfig = new MineOnlineConfig(new File(this.getDataFolder(), "config.yml"));

        Properties propertiesFile = new Properties();

        try {
            propertiesFile.load(new FileInputStream(new File("server.properties")));
            boolean onlineMode = propertiesFile.getProperty("online-mode", "true").equals("true");

            if (onlineMode)
                launchProxy();
        } catch (Exception ex) {
            log.warning("Failed to enable online-mode fix. Authentication may fail.");
        }

        try {
            propertiesFile.load(new FileInputStream(new File("server.properties")));
            String discordToken = mineOnlineConfig.getConfigString("discord-token");
            String discordChannelID = mineOnlineConfig.getConfigString("discord-channel");
            String discordWebhookURL = mineOnlineConfig.getConfigString("discord-webhook-url");
            String serverName = mineOnlineConfig.getConfigString("server-name");


            if (discordToken != null && discordChannelID != null) { // Create the discord bot if token and channel are present
                discord = new DiscordChatBridge(new MinotarAvatarProvider(), discordChannelID, discordToken, discordWebhookURL, new IMessageRecievedListener() {
                    @Override
                    public void onMessageRecieved(MessageReceivedEvent event) {
                        StringBuilder sb = new StringBuilder();
                        String message = event.getMessage().getContentStripped();

                        message = message.replace("\n", "") // Make emojis pretty
                                .replace(("\uD83D\uDE41"), ":)")
                                .replace(("\uD83D\uDE26"), ":(")
                                .replace(("\uD83D\uDE04"), ":D")
                                .replace(("\u2764"), "<3");

                        for (int i = 0; i < message.length(); i++) {
                            char c = message.charAt(i);
                            if ((int) c > 31 && (int) c < 128) {
                                sb.append(c);
                            }
                        }

                        if (event.getMessage().getContentStripped().startsWith("\n"))
                            return;

                        String saneName = event.getAuthor().getName();
                        String saneMessage = sb.toString();

                        if (saneMessage.trim().isEmpty())
                            return;

                        Pattern trailingWhite = Pattern.compile(colorCodeProvider.getColorCode(EColorCodeColor.White) + "\\s{0,}$");
                        Matcher whiteMatcher = trailingWhite.matcher(saneMessage);

                        if (whiteMatcher.find()) { // Prevent a crash in classic where if the message ends with this all connected clients crash
                            saneMessage = saneMessage.substring(0, saneMessage.length() - whiteMatcher.group().length());
                        }

                        if (saneMessage.length() > 256) // Truncate messages that are overly long
                            saneMessage = saneMessage.substring(0, 256);

                        message = (colorCodeProvider.getColorCode(EColorCodeColor.Blue) + saneName + ": " + colorCodeProvider.getColorCode(EColorCodeColor.White) + saneMessage);

                        // remove double color codes that occur with resetting.
                        message = message.replace(colorCodeProvider.getColorCode(EColorCodeColor.White) + colorCodeProvider.getPrefix(), colorCodeProvider.getPrefix());

                        getServer().broadcastMessage(message);
                    }
                }, new IShutdownListener() {
                    @Override
                    public void onShutdown() {
                        discord.sendDiscordMessage("", "Stopping " + serverName);
                    }
                });

                discord.sendDiscordMessage("", "Starting " + serverName);
            }
        } catch (Exception ex) {
            log.warning("Failed to start discord bridge.");
            ex.printStackTrace();
        }

        this.listener = new MineOnlineBroadcastListener(discord);
        this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_LOGIN, this.listener, Event.Priority.Lowest, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, this.listener, Event.Priority.Highest, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_KICK, this.listener, Event.Priority.Highest, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_CHAT, this.listener, Event.Priority.Highest, this);
    }

    @Override
    public void onDisable() {
        if (serverUUID != null) {
            try {
                MineOnlineAPI.deleteServerListing(serverUUID);
            } catch (Exception ex) {

            }
        }

        if (discord != null)
            discord.shutdown();
        broadcastThread.interrupt();
        stopProxy();
    }
}
