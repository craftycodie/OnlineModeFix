import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MineOnlineBroadcast extends JavaPlugin {
    Thread broadcastThread;
    public static long lastPing;
    MineOnlineBroadcastListener listener;
    Logger log;

    public static String[] readUsersFile(String path) {
        try {
            File usersFile = new File(path);
            if (usersFile.exists()) {
                LinkedList list = new LinkedList();
                BufferedReader reader = new BufferedReader(new FileReader(usersFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    list.add(line);
                }
                reader.close();

                return (String[])list.toArray(new String[0]);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return new String[0];
    }

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

    public static String listServer(
            String ip,
            String port,
            int users,
            String maxUsers,
            String name,
            boolean onlineMode,
            String md5,
            boolean whitelisted,
            String[] whitelistUsers,
            String[] whitelistIPs,
            String[] whitelistUUIDs,
            String[] bannedUsers,
            String[] bannedIPs,
            String[] bannedUUIDs,
            String owner,
            String[] playerNames
    ) {
        HttpURLConnection connection = null;

        try {
            JSONObject jsonObject = new JSONObject();
            if (ip != null)
                jsonObject.put("ip", ip);
            jsonObject.put("port", port);
            if (users > -1)
                jsonObject.put("users", users);
            jsonObject.put("max", maxUsers);
            jsonObject.put("name", name);
            jsonObject.put("onlinemode", onlineMode);
            jsonObject.put("md5", md5);
            jsonObject.put("whitelisted", whitelisted);
            jsonObject.put("whitelistUsers", whitelistUsers);
            jsonObject.put("whitelistIPs", whitelistIPs);
            jsonObject.put("whitelistUUIDs", whitelistUUIDs);
            jsonObject.put("bannedUsers", bannedUsers);
            jsonObject.put("bannedIPs", bannedIPs);
            jsonObject.put("bannedUUIDs", bannedUUIDs);
            if(owner != null)
                jsonObject.put("owner", owner);
            jsonObject.put("players", playerNames);

            String json = jsonObject.toString();

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

            JSONObject resObject = new JSONObject(response.toString());
            return resObject.has("uuid") ? resObject.getString("uuid") : null;
        } catch (Exception e) {

            e.printStackTrace();
            return null;
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

                            String ip = propertiesFile.getProperty("server-ip", null);
                            String port = propertiesFile.getProperty("server-port", "25565");
                            int users = getServer().getOnlinePlayers().length;
                            String maxUsers = propertiesFile.getProperty("max-players", "20");
                            String name = propertiesFile.getProperty("server-name", "Minecraft Server");
                            boolean onlineMode = true; // Assume Authme is in use for now.
                            String md5 = propertiesFile.getProperty("version-md5", "");
                            boolean whitelisted = propertiesFile.getProperty("whitelist", "false").equals("true");
                            String[] whitelistUsers = new String[0];

                            if (whitelisted) {
                                whitelistUsers = readUsersFile("white-list.txt");
                            }

                            String[] bannedUsers = readUsersFile("banned-players.txt");
                            String[] bannedIPs = readUsersFile("banned-ips.txt");

                            String owner = null;
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
                                    whitelistUsers,
                                    new String[0],
                                    new String[0],
                                    bannedUsers,
                                    bannedIPs,
                                    new String[0],
                                    owner,
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
