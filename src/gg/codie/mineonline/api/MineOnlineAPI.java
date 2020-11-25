package gg.codie.mineonline.api;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;

public class MineOnlineAPI {
    public static void deleteServerListing(String uuid) throws IOException {
        HttpURLConnection connection;

        URL url = new URL("https://mineonline.codie.gg/api/servers/" + uuid);
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.connect();

        if (connection != null)
            connection.disconnect();
    }

    public static String listServer(
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
            boolean useBetaEvolutionsAuth,
            String serverIcon
    ) {
        HttpURLConnection connection = null;

        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[]{MineOnlineAPI.class.getProtectionDomain().getCodeSource().getLocation()});

            Class jsonObjectClass = classLoader.loadClass("org.json.JSONObject");

            Constructor jsonObjectConstructor = jsonObjectClass.getConstructor();
            Method jsonObjectPut = jsonObjectClass.getMethod("put", String.class, Object.class);
            Method jsonObjectHas = jsonObjectClass.getMethod("has", String.class);
            Method jsonObjectGetString = jsonObjectClass.getMethod("getString", String.class);
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
            jsonObjectPut.invoke(jsonObject, "md5", md5.toUpperCase());
            jsonObjectPut.invoke(jsonObject, "whitelisted", whitelisted);
            if (!dontListPlayers)
                jsonObjectPut.invoke(jsonObject, "players", playerNames);
            jsonObjectPut.invoke(jsonObject, "motd", motd);
            jsonObjectPut.invoke(jsonObject, "dontListPlayers", dontListPlayers);
            jsonObjectPut.invoke(jsonObject, "useBetaEvolutionsAuth", useBetaEvolutionsAuth);
            jsonObjectPut.invoke(jsonObject, "serverIcon", serverIcon);

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


            jsonObjectConstructor = jsonObjectClass.getConstructor(String.class);

            Object resObject = jsonObjectConstructor.newInstance(response.toString());
            if ((boolean)jsonObjectHas.invoke(resObject, "uuid")) {
                return (String)jsonObjectGetString.invoke(resObject, "uuid");
            } else {
                return null;
            }
        } catch (Exception e) {

            e.printStackTrace();
        } finally {

            if (connection != null)
                connection.disconnect();
        }

        return null;
    }
}
