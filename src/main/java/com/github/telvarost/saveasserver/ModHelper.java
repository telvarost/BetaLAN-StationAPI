package com.github.telvarost.saveasserver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ModHelper {

    /**
     * Copy a file from source to destination.
     *
     * @param source
     *        the source
     * @param destination
     *        the destination
     * @return True if succeeded , False if not
     */
    public static boolean copy(InputStream source , String destination) {
        boolean succeess = true;

        System.out.println("Copying ->" + source + "\n\tto ->" + destination);

        try {
            Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            System.out.println("Failed to extract server jar file: " + ex.toString());
            succeess = false;
        }

        return succeess;

    }

    public static class ModHelperFields {
        public static Process CurrentServer = null;
        public static String CurrentWorldFolder = "";
    }
}
