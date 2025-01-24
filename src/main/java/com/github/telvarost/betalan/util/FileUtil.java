package com.github.telvarost.betalan.util;

import com.github.telvarost.betalan.BetaLAN;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("UnusedReturnValue")
public class FileUtil {
    /**
     * Copy a file from source to destination.
     *
     * @param source      the source
     * @param destination the destination
     * @return True if succeeded , False if not
     */
    public static boolean copy(InputStream source, String destination) {
        BetaLAN.LOGGER.info("Copying " + source + " to " + destination);

        try {
            Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            BetaLAN.LOGGER.error("Failed to copy a file", ex);
            return false;
        }

        return true;
    }

    /**
     * Adds all the files in the supplied list into an archive
     *
     * @param zipFile The path of the archive to put the files into
     * @param fileList The files to putin the archive
     */
    public static void zipFiles(String zipFile, ArrayList<String> fileList) {
        File savesDir = new File(Minecraft.getRunDirectory(), "saves");
        File worldDir = new File(savesDir, BetaLAN.CurrentWorldFolder);
        byte[] buffer = new byte[1024];
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);
            
            BetaLAN.LOGGER.info("Zipping the world into " + zipFile);
            FileInputStream in = null;

            int fileListLength = (null != fileList) ? fileList.size() : 0;
            for (int fileIndex = 0; fileIndex < fileListLength; fileIndex++) {
                BetaLAN.LOGGER.info("Zipping file " + fileList.get(fileIndex));
                ZipEntry ze = new ZipEntry(fileList.get(fileIndex));
                zos.putNextEntry(ze);
                try {
                    in = new FileInputStream(worldDir.getAbsolutePath().replaceAll("\\\\", "/") + File.separator + fileList.get(fileIndex));
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }

            zos.closeEntry();

        } catch (IOException ex) {
            BetaLAN.LOGGER.error("Error when zipping " + zipFile, ex);
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
                BetaLAN.LOGGER.error("Error when closing the file stream while zipping " + zipFile, e);
            }
        }
    }

    /**
     * Fills the supplied ArrayList with all files in the given node and its subdirectories
     *
     * @param node     The "node" to start with
     * @param fileList The ArrayList to fill
     */
    public static void generateFileList(File node, ArrayList<String> fileList) {
        // add file only
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.getAbsolutePath().replaceAll("\\\\", "/")));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            assert subNote != null;
            for (String filename : subNote) {
                generateFileList(new File(node, filename), fileList);
            }
        }
    }

    private static String generateZipEntry(String file) {
        File savesDir = new File(Minecraft.getRunDirectory(), "saves");
        File worldDir = new File(savesDir, BetaLAN.CurrentWorldFolder);
        return file.substring(worldDir.getAbsolutePath().replaceAll("\\\\", "/").length() + 1);
    }
}
