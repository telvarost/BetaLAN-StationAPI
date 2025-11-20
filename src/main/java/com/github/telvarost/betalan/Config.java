package com.github.telvarost.betalan;

import com.github.telvarost.betalan.util.DefaultGamemodeEnum;
import net.glasslauncher.mods.gcapi3.api.ConfigCategory;
import net.glasslauncher.mods.gcapi3.api.ConfigEntry;
import net.glasslauncher.mods.gcapi3.api.ConfigRoot;

public class Config {

    @ConfigRoot(value = "config", visibleName = "BetaLAN")
    public static ConfigFields config = new ConfigFields();

    public static class ConfigFields {

        @ConfigCategory(name = "Advanced Server Config")
        public final AdvancedServerConfig ADVANCED_SERVER_CONFIG = new AdvancedServerConfig();

        @ConfigEntry(name = "Automatically OP LAN Server Host")
        public Boolean AUTO_OP_LAN_SERVER_HOST = true;

        @ConfigEntry(name = "Backup World On Server Launch")
        public Boolean BACKUP_WORLD_ON_LAN_SERVER_LAUNCH = true;

        @ConfigEntry(
                name = "Enable World Showcase Mode",
                description = "World/Player changes will not be saved"
        )
        public Boolean ENABLE_WORLD_SHOWCASE_MODE = false;

        @ConfigEntry(name = "Force \"online-mode\" To False")
        public Boolean FORCE_ONLINEMODE_FALSE = true;

        @ConfigEntry(name = "Java Path", description = "Path to Java 17 runtime", maxLength = 32767)
        public String JAVA_PATH = "java";

        @ConfigEntry(name = "Provide Server Logs In Console")
        public Boolean enableServerLogsInConsole = true;

        @ConfigEntry(name = "Server GUI Enabled", description = "Helps with issuing commands and monitoring")
        public Boolean SERVER_GUI_ENABLED = false;

        @ConfigEntry(name = "Server Port", maxLength = 65535)
        public Integer SERVER_PORT = 25565;
    }

    public static class AdvancedServerConfig {

        @ConfigEntry(name = "Allow Flight")
        public Boolean ALLOW_FLIGHT = false;

        @ConfigEntry(name = "Allow Nether", description = "If false do not 'Open to LAN' in the nether")
        public Boolean ALLOW_NETHER = true;

        @ConfigEntry(name = "Default Gamemode", description = "Requires BHCreative to use default creative")
        public DefaultGamemodeEnum DEFAULT_GAMEMODE = DefaultGamemodeEnum.SURVIVAL;

        @ConfigEntry(name = "Enable PVP")
        public Boolean ENABLE_PVP = true;

        @ConfigEntry(name = "Enable White-list")
        public Boolean ENABLE_WHITELIST = false;

        @ConfigEntry(name = "Max Players", maxLength = 256)
        public Integer MAX_PLAYERS = 20;

        @ConfigEntry(name = "Spawn Animals")
        public Boolean SPAWN_ANIMALS = true;

        @ConfigEntry(name = "Spawn Protection Radius", maxLength = 256, description = "Requires UniTweaks to work")
        public Integer SPAWN_PROTECTION_RADIUS = 16;

        @ConfigEntry(name = "View Distance", maxLength = 256)
        public Integer VIEW_DISTANCE = 10;
    }
}
