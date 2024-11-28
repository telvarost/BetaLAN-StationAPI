package com.github.telvarost.saveasserver;

import net.glasslauncher.mods.gcapi3.api.*;

public class Config {

    @ConfigRoot(value = "config", visibleName = "SaveAsServer")
    public static ConfigFields config = new ConfigFields();

    public static class ConfigFields {

        @ConfigEntry(
                name = "Server GUI Enabled"
        )
        public Boolean SERVER_GUI_ENABLED = true;

        @ConfigEntry(
                name = "Java Path",
                description = "Path to Java 17 runtime",
                maxLength = 32767
        )
        public String JAVA_PATH = "java";
    }
}
