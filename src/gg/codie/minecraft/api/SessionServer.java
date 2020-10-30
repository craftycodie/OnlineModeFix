package gg.codie.minecraft.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SessionServer {
    private static final String BASE_URL = "https://sessionserver.mojang.com";

    public static boolean hasJoined(String username, String serverId, String ip) throws IOException {
        HttpURLConnection connection;

        URL url = new URL(BASE_URL + "/session/minecraft/hasJoined?username=" + username + "&serverId=" + serverId + (ip != null ? "&ip=" + ip : ""));
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setDoOutput(false);

        connection.connect();

        return connection.getResponseCode() == 200;
    }
}
