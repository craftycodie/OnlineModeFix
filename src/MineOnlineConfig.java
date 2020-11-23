import java.io.File;

public class MineOnlineConfig {
    private boolean newFile = false;
    private PropertiesFile propertiesFile;

    public MineOnlineConfig(File file) {
        if (!file.exists()) {
            newFile = true;
            file.getParentFile().mkdirs();
        }
        propertiesFile = new PropertiesFile(file.getAbsolutePath());
        write();
        propertiesFile.save();
    }
    public void write() {
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
    }

    //Getters Start
    public Object getConfigOption(String key) {
        return this.propertiesFile.getProperty(key);
    }

    public String getConfigString(String key) {
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


    private void generateConfigOption(String key, Object defaultValue) {
        if (propertiesFile.getProperty(key) == null) {
            propertiesFile.setString(key, String.valueOf(defaultValue));
        }
        final Object value = propertiesFile.getProperty(key);
        propertiesFile.removeKey(key);
        propertiesFile.setString(key, String.valueOf(value));
    }




}
