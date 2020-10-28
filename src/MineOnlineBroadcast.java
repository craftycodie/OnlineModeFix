import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MineOnlineBroadcast extends JavaPlugin {
    Thread broadcastThread;
    public static long lastPing;
    MineOnlineBroadcastListener listener;
    Logger log;

    public static byte[] createChecksum(String filename) throws Exception {
        InputStream fis =  new FileInputStream(filename);

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
            String maxUsers,
            String name,
            boolean onlineMode,
            String md5,
            boolean whitelisted,
            String[] playerNames
    ) {
        HttpURLConnection connection = null;

        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[] { MineOnlineBroadcast.class.getProtectionDomain().getCodeSource().getLocation() });

            Class jsonObjectClass = classLoader.loadClass("org.json.JSONObject");

            Constructor jsonObjectConstructor = jsonObjectClass.getConstructor();
            Method jsonObjectPut = jsonObjectClass.getMethod("put", String.class, Object.class);
            Method jsonObjectToString = jsonObjectClass.getMethod("toString");

            Object jsonObject = jsonObjectConstructor.newInstance();
            if (ip != null)
                jsonObjectPut.invoke(jsonObject, "ip", ip);
            jsonObjectPut.invoke(jsonObject, "port", port);
            if (users > -1)
                jsonObjectPut.invoke(jsonObject, "users", users);
            jsonObjectPut.invoke(jsonObject, "max", maxUsers);
            jsonObjectPut.invoke(jsonObject, "name", name);
            jsonObjectPut.invoke(jsonObject, "onlinemode", onlineMode);
            jsonObjectPut.invoke(jsonObject, "md5", md5);
            jsonObjectPut.invoke(jsonObject, "whitelisted", whitelisted);
            jsonObjectPut.invoke(jsonObject, "players", playerNames);

            String json = (String)jsonObjectToString.invoke(jsonObject);

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

        this.log.info("Enabled MineOnlineBroadcast");

        broadcastThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    if (System.currentTimeMillis() - MineOnlineBroadcast.lastPing > 45000) {
                        try {
                            lastPing = System.currentTimeMillis();
                            Properties propertiesFile = new Properties();
                            propertiesFile.load(new FileInputStream(new File("server.properties")));

                            String ip = propertiesFile.getProperty("serverlist-ip", propertiesFile.getProperty("server-ip", propertiesFile.getProperty("ip", null)));
                            String port = propertiesFile.getProperty("serverlist-port", propertiesFile.getProperty("server-port", propertiesFile.getProperty("port", "25565")));
                            int users = getServer().getOnlinePlayers().length;
                            String maxUsers = propertiesFile.getProperty("max-players", "20");
                            String name = propertiesFile.getProperty("server-name", "Minecraft Server");
                            boolean onlineMode = propertiesFile.getProperty("online-mode", "true").equals("true");
                            String md5 = propertiesFile.getProperty("version-md5", "");
                            boolean whitelisted = propertiesFile.getProperty("whitelist", "false").equals("true");

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
                                    playerNames
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
        this.listener = new MineOnlineBroadcastListener();
        this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_LOGIN, this.listener, Event.Priority.Lowest, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, this.listener, Event.Priority.Highest, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_KICK, this.listener, Event.Priority.Highest, this);
    }

    @Override
    public void onDisable() {
        broadcastThread.interrupt();
    }
}
