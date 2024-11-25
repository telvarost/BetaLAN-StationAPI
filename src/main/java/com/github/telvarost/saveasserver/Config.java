package com.github.telvarost.saveasserver;

import net.glasslauncher.mods.gcapi3.api.*;

public class Config {

    @ConfigRoot(value = "config", visibleName = "SaveAsServer")
    public static ConfigFields config = new ConfigFields();

    public static class ConfigFields {

        @ConfigEntry(
                name = "Test Config",
                multiplayerSynced = true
        )
        public Boolean CONFIG_TEST = true;
    }
}
