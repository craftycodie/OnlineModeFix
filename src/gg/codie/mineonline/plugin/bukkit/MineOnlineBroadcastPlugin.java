package gg.codie.mineonline.plugin.bukkit;

import gg.codie.common.input.EColorCodeColor;
import gg.codie.minecraft.server.MinecraftColorCodeProvider;
import gg.codie.mineonline.discord.DiscordChatBridge;
import gg.codie.mineonline.discord.IMessageRecievedListener;
import gg.codie.mineonline.discord.IShutdownListener;
import gg.codie.mineonline.discord.MinotarAvatarProvider;
import gg.codie.mineonline.plugin.ProxyThread;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
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

    public static void listServer(
            String ip,
            String port,
            int users,
            int maxUsers,
            String name,
            boolean onlineMode,
            String md5,
            boolean whitelisted,
            String[] playerNames,
            String motd,
            boolean dontListPlayers,
            boolean useBetaEvolutionsAuth
    ) {
        HttpURLConnection connection = null;

        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[]{MineOnlineBroadcastPlugin.class.getProtectionDomain().getCodeSource().getLocation()});

            Class jsonObjectClass = classLoader.loadClass("org.json.JSONObject");

            Constructor jsonObjectConstructor = jsonObjectClass.getConstructor();
            Method jsonObjectPut = jsonObjectClass.getMethod("put", String.class, Object.class);
            Method jsonObjectToString = jsonObjectClass.getMethod("toString");

            Object jsonObject = jsonObjectConstructor.newInstance();
            if (ip != null)
                jsonObjectPut.invoke(jsonObject, "ip", ip);
            jsonObjectPut.invoke(jsonObject, "port", port);
            if (users > -1 && !dontListPlayers)
                jsonObjectPut.invoke(jsonObject, "users", users);
            jsonObjectPut.invoke(jsonObject, "max", maxUsers);
            jsonObjectPut.invoke(jsonObject, "name", name);
            jsonObjectPut.invoke(jsonObject, "onlinemode", onlineMode);
            jsonObjectPut.invoke(jsonObject, "md5", md5);
            jsonObjectPut.invoke(jsonObject, "whitelisted", whitelisted);
            if (!dontListPlayers)
                jsonObjectPut.invoke(jsonObject, "players", playerNames);
            jsonObjectPut.invoke(jsonObject, "motd", motd);
            jsonObjectPut.invoke(jsonObject, "dontListPlayers", dontListPlayers);
            jsonObjectPut.invoke(jsonObject, "useBetaEvolutionsAuth", useBetaEvolutionsAuth);

            String json = (String) jsonObjectToString.invoke(jsonObject);

            URL url = new URL("https://mineonline.codie.gg/api/servers");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    @Override
    public void onEnable() {
        initialize();

        this.log = Logger.getLogger("Minecraft");

        this.log.info("Enabled gg.codie.mineonline.plugin.bukkit.MineOnlineBroadcast");

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

                            listServer(
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
                                    useBetaEvolutionsAuth
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

//    private void unregister() {
//        if(registeredListener != null)
//            etc.getLoader().removeListener(registeredListener);
//    }

    @Override
    public void onDisable() {
        //unregister();
        if (discord != null)
            discord.shutdown();
        broadcastThread.interrupt();
        stopProxy();
    }
}
