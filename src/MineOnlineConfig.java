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
        generateConfigOption("server-name", "Minecraft Server");
        generateConfigOption("version-md", "");
        generateConfigOption("dont-list-players", false);
        generateConfigOption("serverlist-motd", null);
        generateConfigOption("beta-evolutions-support", false);
        //Discord
        generateConfigOption("discord-token", null);
        generateConfigOption("discord-channel", null);
        generateConfigOption("discord-webhook-url", null);

    }

    //Getters Start
    public Object getConfigOption(String key) {
        return this.propertiesFile.getProperty(key);
    }

    public String getConfigString(String key) {
        if(getConfigOption(key) == null) {
            return null;
        }
        //Hacky solution for hmod config storing null as text
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


    private void generateConfigOption(String key, Object defaultValue) {
        if (propertiesFile.getProperty(key) == null) {
            propertiesFile.setString(key, String.valueOf(defaultValue));
        }
        final Object value = propertiesFile.getProperty(key);
        propertiesFile.removeKey(key);
        propertiesFile.setString(key, String.valueOf(value));
    }




}
