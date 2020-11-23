package gg.codie.mineonline.plugin.bukkit;

import org.bukkit.util.config.Configuration;

import java.io.File;

public class MineOnlineConfig extends Configuration {


    public MineOnlineConfig(File file) {
        super(file);
        this.reload();
    }

    private void write() {
        //Main
        generateConfigOption("config-version", 1);
        //Setting
        generateConfigOption("public", true);
        generateConfigOption("serverlist-ip", null);
        generateConfigOption("serverlist-port", 25565);
        generateConfigOption("max-players", 20);
        generateConfigOption("server-name", "Minecraft Server");
        generateConfigOption("online-mode", true);
        generateConfigOption("version-md", "");
        generateConfigOption("whitelist", false);
        generateConfigOption("dont-list-players", false);
        generateConfigOption("serverlist-motd", null);
        //Discord
        generateConfigOption("discord-token", null);
        generateConfigOption("discord-channel", null);
        generateConfigOption("discord-webhook-url", null);
    }

    private void generateConfigOption(String key, Object defaultValue) {
        if (this.getProperty(key) == null) {
            this.setProperty(key, defaultValue);
        }
        final Object value = this.getProperty(key);
        this.removeProperty(key);
        this.setProperty(key, value);
    }

    //Getters Start
    public Object getConfigOption(String key) {
        return this.getProperty(key);
    }

    public String getConfigString(String key) {
        if(getConfigOption(key) == null) {
            return null;
        }
        //Hacky solution
        if(String.valueOf(getConfigOption(key)).equalsIgnoreCase("null")) {
            return null;
        }


        return String.valueOf(getConfigOption(key));
    }

    public Integer getConfigInteger(String key) {
        return Integer.valueOf(getConfigString(key));
    }

    public Long getConfigLong(String key) {
        return Long.valueOf(getConfigString(key));
    }

    public Double getConfigDouble(String key) {
        return Double.valueOf(getConfigString(key));
    }

    public Boolean getConfigBoolean(String key) {
        return Boolean.valueOf(getConfigString(key));
    }


    //Getters End


    private void reload() {
        this.load();
        this.write();
        this.save();
    }
}