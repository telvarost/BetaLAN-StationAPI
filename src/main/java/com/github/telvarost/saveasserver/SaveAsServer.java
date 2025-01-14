package com.github.telvarost.saveasserver;

import net.modificationstation.stationapi.api.mod.entrypoint.Entrypoint;
import net.modificationstation.stationapi.api.util.Null;
import org.apache.logging.log4j.Logger;

public class SaveAsServer {
    @Entrypoint.Logger
    public static final Logger LOGGER = Null.get();
    
    public static boolean isLanServer = false;
    public static String CurrentWorldFolder = "";
}
