package com.github.telvarost.betalan;

import net.glasslauncher.mods.gcapi3.api.ConfigEntry;
import net.glasslauncher.mods.gcapi3.api.ConfigRoot;

public class Config {

    @ConfigRoot(value = "config", visibleName = "BetaLAN")
    public static ConfigFields config = new ConfigFields();

    public static class ConfigFields {

        @ConfigEntry(name = "Automatically OP LAN Server Host")
        public Boolean AUTO_OP_LAN_SERVER_HOST = true;

        @ConfigEntry(name = "Backup World On Server Launch")
        public Boolean BACKUP_WORLD_ON_LAN_SERVER_LAUNCH = true;

        @ConfigEntry(name = "Force \"online-mode\" To False")
        public Boolean FORCE_ONLINEMODE_FALSE = true;

        @ConfigEntry(name = "Java Path", description = "Path to Java 17 runtime", maxLength = 32767)
        public String JAVA_PATH = "java";

        @ConfigEntry(name = "Server GUI Enabled", description = "Helps with issuing commands and monitoring")
        public Boolean SERVER_GUI_ENABLED = false;

        @ConfigEntry(name = "Server Port", maxLength = 65535)
        public Integer SERVER_PORT = 25565;

        @ConfigEntry(name = "Enable Server logs in console")
        public Boolean enableServerLogsInConsole = true;
    }
}
