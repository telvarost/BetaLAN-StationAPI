package com.github.telvarost.saveasserver;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings({"deprecation", "DanglingJavadoc", "ResultOfMethodCallIgnored", "IndexOfReplaceableByContains", "ConditionCoveredByFurtherCondition", "CallToPrintStackTrace"})
public class LocalServerManager {
    private static LocalServerManager INSTANCE;

    // The status of the server
    public ServerStatus status;
    public Process serverProcess;
    public BufferedReader processOutput;

    // Loading Bar
    String loadingText = "";
    int loadingProgress = 0;

    private List<String> fileList;
    public Minecraft minecraft;

    public LocalServerManager() {
        minecraft = (Minecraft) FabricLoader.getInstance().getGameInstance();
        status = ServerStatus.NOT_STARTED;
    }

    public static LocalServerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LocalServerManager();
        }

        return INSTANCE;
    }

    public void start() {
        status = ServerStatus.INITIALIZING;
    }

    public void stop() {
        status = ServerStatus.NOT_STARTED;
    }

    private int parseInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception var4) {
            return defaultValue;
        }
    }

    public void run() {
        switch (status) {
            case NOT_STARTED -> {

            }

            case INITIALIZING -> {
                if (Config.config.BACKUP_WORLD_ON_LAN_SERVER_LAUNCH) {
                    status = ServerStatus.BACKUP;
                } else {
                    status = ServerStatus.LAUNCHING;
                }
            }

            case BACKUP -> {
                /** - Prepare and zip world files */
                fileList = new ArrayList<String>();
                File savesDir = new File(Minecraft.getRunDirectory(), "saves");
                File worldDir = new File(savesDir, ModHelper.ModHelperFields.CurrentWorldFolder);
                generateFileList(worldDir);
                zipIt("saves" + File.separator + "_" + ModHelper.ModHelperFields.CurrentWorldFolder + ".zip");

                System.out.println("World backup successfully created!");
                /** - Set flag letting server know that it can now launch */
                status = ServerStatus.LAUNCHING;
            }

            case LAUNCHING -> {
                /** - Create server lock */
                File savesDir = new File(Minecraft.getRunDirectory(), "saves");
                File worldDir = new File(savesDir, ModHelper.ModHelperFields.CurrentWorldFolder);
                File serverLock = new File(worldDir, "server.lock");
                if (!serverLock.exists()) {
                    try {
                        serverLock.createNewFile();
                    } catch (IOException e) {
                        System.out.println("Failed to create server lock file! Client player may be de-synced after launch!");
                    }
                }

                /** - Prepare logging folder */
                File[] files = new File("logging").listFiles();
                if (files != null) {
                    for (File currentFile : files) {
                        currentFile.delete();
                    }
                }

                /** - Update loading bar */
                loadingText = "Preparing world";
                loadingProgress = 0;

                /** - Launch server */
                String argNoGui = (Config.config.SERVER_GUI_ENABLED) ? "" : "nogui";
                ProcessBuilder pb = new ProcessBuilder(Config.config.JAVA_PATH, "-jar", "local-babric-server.0.16.9.jar", argNoGui);
                pb.directory(Minecraft.getRunDirectory());

                //pb.inheritIO();
                //pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
                //pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                //pb.redirectError(ProcessBuilder.Redirect.INHERIT);

                RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
                String javaExecutablePath = runtimeMXBean.getInputArguments().toString();
                String[] inputArgs = javaExecutablePath.split(" ");
                String javaPath = inputArgs[0];
                System.out.println("Current Java Executable Path: " + javaPath);
                System.out.println("AA");
                
                try {
                    serverProcess = pb.start();

                    processOutput = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));
                    serverProcess.getErrorStream().close();
                    serverProcess.getOutputStream().close();

                    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                        public void run() {
                            if (serverProcess != null) {
                                Minecraft client = (Minecraft) FabricLoader.getInstance().getGameInstance();
                                if (client != null) {
                                    client.setScreen(new TitleScreen());
                                }
                            }
                        }
                    }, "ServerCrashMonitor-thread"));

//                    serverProcess.getErrorStream().close();
//                    serverProcess.getInputStream().close();
//                    serverProcess.getOutputStream().close();
                } catch (IOException ex) {
                    this.minecraft.setScreen(new TitleScreen());
                    System.out.println("Failed to open client world to LAN: " + ex.toString());
                }
                status = ServerStatus.PREPARING;
            }

            case PREPARING -> {

                try {
                    if (processOutput.ready()) {
                        System.out.println("SERVER OUTPUT: " + processOutput.readLine());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

//                File saveAsServerBegin = new File("logging" + File.separator + "preparing-level");
//                if (saveAsServerBegin.exists()) {
//                    saveAsServerBegin.delete();
//
//                    System.out.println("Preparing LAN server...");
//                }
                //status = ServerStatus.LOADING;
            }

            case LOADING -> {
                File[] files = new File("logging").listFiles();
                String searchLevel = "loading-level";
                String searchProgress = "level-progress";
                if (files != null) {
                    for (File currentFile : files) {
                        String fileName = currentFile.getName();

                        if (fileName.toLowerCase().indexOf(searchLevel.toLowerCase()) != -1) {
                            String levelString = fileName.substring("loading-level-".length());
                            loadingText = "Preparing start region for level " + levelString;
                            loadingProgress = 0;
                            currentFile.delete();
                        }

                        if (fileName.toLowerCase().indexOf(searchProgress.toLowerCase()) != -1) {
                            String progressString = fileName.substring("level-progress-".length());
                            loadingProgress = parseInt(progressString, loadingProgress);
                            currentFile.delete();
                        }
                    }
                }
                loadedTime = System.currentTimeMillis();
                status = ServerStatus.STARTED;
            }

            case STARTED -> {
                //File saveAsServerEnd = new File("logging" + File.separator + "done-loading");
                //if (saveAsServerEnd.exists()) {
                loadingProgress = 100;
                //saveAsServerEnd.delete();

                /** - Have the client join the local server */
                loadingText = "Waiting for server to load";

                if (System.currentTimeMillis() - loadedTime > 10000) {
                    System.out.println("Done loading LAN server!");
                    joinLocalServer();
                    status = ServerStatus.RUNNING;
                }
                //}
            }

            case RUNNING -> {

            }
        }
    }

    long loadedTime = 0;

    private void joinLocalServer() {
        String serverAddress = "127.0.0.1:" + Config.config.SERVER_PORT;
        this.minecraft.options.lastServer = serverAddress.replaceAll(":", "_");
        this.minecraft.options.save();
        String[] var3 = serverAddress.split(":");
        if (serverAddress.startsWith("[")) {
            int var4 = serverAddress.indexOf("]");
            if (var4 > 0) {
                String var5 = serverAddress.substring(1, var4);
                String var6 = serverAddress.substring(var4 + 1).trim();
                if (!var6.isEmpty() && var6.startsWith(":")) {
                    var6 = var6.substring(1);
                    var3 = new String[]{var5, var6};
                } else {
                    var3 = new String[]{var5};
                }
            }
        }

        if (var3.length > 2) {
            var3 = new String[]{serverAddress};
        }

        this.minecraft.setScreen(new ConnectScreen(this.minecraft, var3[0], var3.length > 1 ? this.parseInt(var3[1], 25565) : 25565));
    }

    public void prepareAndLaunchServer(PlayerEntity player) {
        try {
            /** - Get server files */
            File savesDir = new File(Minecraft.getRunDirectory(), "saves");
            File worldDir = new File(savesDir, ModHelper.ModHelperFields.CurrentWorldFolder);
            File playerDataDir = new File(worldDir, "players");
            if (!playerDataDir.exists()) {
                playerDataDir.mkdirs();
            }

            /** - Save client player data to server player file */
            NbtCompound var2 = new NbtCompound();
            player.write(var2);
            File tempPlayerFile = new File(playerDataDir, "_tmp_.dat");
            File playerFile = new File(playerDataDir, this.minecraft.session.username + ".dat");
            NbtIo.writeCompressed(var2, new FileOutputStream(tempPlayerFile));
            if (playerFile.exists()) {
                playerFile.delete();
            }
            tempPlayerFile.renameTo(playerFile);

            /** - Create/edit LAN server OP list */
            if (Config.config.AUTO_OP_LAN_SERVER_HOST) {
                File serverOpListFile = new File(Minecraft.getRunDirectory(), "ops.txt");
                if (!serverOpListFile.exists()) {
                    /** - LAN server OP list file does not exist, create new LAN server OP list file */
                    createLocalServerOpListFile(serverOpListFile);
                } else {
                    /** - LAN server OP list exists, edit LAN server OP list file */
                    editLocalServerOpListFile(serverOpListFile);
                }
            }

            /** - Create/edit LAN server properties */
            File serverPropertiesFile = new File(Minecraft.getRunDirectory(), "server.properties");
            if (!serverPropertiesFile.exists()) {
                /** - LAN server properties file does not exist, create new LAN server properties file */
                createLocalServerPropertiesFile(serverPropertiesFile);
            } else {
                /** - LAN server properties exists, edit LAN server properties file */
                editLocalServerPropertiesFile(serverPropertiesFile);
            }

            /** - Extract server jar file */
            File storedServerJar = new File(Minecraft.getRunDirectory().getAbsolutePath() + File.separator + "local-babric-server.0.16.9.jar");
            if (!storedServerJar.exists()) {
                ModHelper.copy(getClass().getResourceAsStream("/assets/saveasserver/local-babric-server.0.16.9.jar")
                        , Minecraft.getRunDirectory().getAbsolutePath() + File.separator + "local-babric-server.0.16.9.jar");
            }

            /** - Create file telling the server it is a local server */
            File clientLockFile = new File(Minecraft.getRunDirectory(), "client.lock");
            if (!clientLockFile.exists()) {
                clientLockFile.createNewFile();
            }

            /** - Prepare folder for server logging info to client */
            File saveAsServerFolder = new File(Minecraft.getRunDirectory(), "logging");
            if (!saveAsServerFolder.exists()) {
                saveAsServerFolder.mkdirs();
            }

            /** - Close client world */
            this.minecraft.stats.increment(Stats.LEAVE_GAME, 1);
            if (this.minecraft.isWorldRemote()) {
                this.minecraft.world.disconnect();
            }
            this.minecraft.setWorld((World) null);
            this.minecraft.setScreen(new JoinLocalServerScreen(null));
            //this.minecraft.setScreen(new TitleScreen());
        } catch (Exception ex) {
            System.out.println("Failed to open client world to LAN: " + ex.toString());
        }
    }

    private void createLocalServerOpListFile(File serverOpListFile) {
        try {
            PrintWriter writer = new PrintWriter(serverOpListFile, StandardCharsets.UTF_8);
            writer.println(this.minecraft.session.username);

            /** - Release file resources */
            writer.close();
        } catch (IOException e) {
            System.out.println("Failed to create LAN server OP list: ");
            e.printStackTrace();
        }
    }

    private void editLocalServerOpListFile(File serverOpListFile) {
        File tempServerOpListFile = new File(Minecraft.getRunDirectory(), "_ops.txt");
        try {
            Files.copy(serverOpListFile.toPath(), tempServerOpListFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            FileWriter writer = new FileWriter(tempServerOpListFile);
            Reader reader = new FileReader(tempServerOpListFile);
            BufferedReader br = new BufferedReader(reader);
            boolean isClientPlayerOp = false;

            /** - Scan file for values to change */
            while (br.ready()) {
                String currentLine = br.readLine();

                /** - Check if client player is OP */
                if (currentLine.contains(this.minecraft.session.username)) {
                    isClientPlayerOp = true;
                }
            }

            /** - Client player is not OP, OP client player */
            if (!isClientPlayerOp) {
                writer.write(this.minecraft.session.username + "\n");
            }

            /** - Release file resources */
            writer.close();
            br.close();
            reader.close();

            /** - Replace current OP list file with newly created OP list file */
            tempServerOpListFile.renameTo(serverOpListFile);
        } catch (IOException e) {
            System.out.println("Failed to update LAN server properties: ");
            e.printStackTrace();
        }

        /** - Ensure temporary OP list file is deleted if it is no longer needed */
        if (serverOpListFile.exists() && tempServerOpListFile.exists()) {
            tempServerOpListFile.delete();
        }
    }

    private void createLocalServerPropertiesFile(File serverPropertiesFile) {
        try {
            PrintWriter writer = new PrintWriter(serverPropertiesFile, StandardCharsets.UTF_8);
            writer.println("#Minecraft server properties");
            writer.println("#" + new Date());
            if (null != ModHelper.ModHelperFields.CurrentWorldFolder) {
                writer.println("level-name=./saves/" + ModHelper.ModHelperFields.CurrentWorldFolder);
            } else {
                writer.println("#level-name=world");
            }
            writer.println("default-gamemode=0");
            writer.println("view-distance=10");
            writer.println("white-list=false");
            writer.println("server-ip=");
            writer.println("pvp=true");
            writer.println("level-seed=");
            writer.println("spawn-animals=true");
            writer.println("server-port=" + Config.config.SERVER_PORT);
            writer.println("allow-nether=true");
            writer.println("spawn-monsters=true");
            writer.println("max-players=20");
            if (Config.config.FORCE_ONLINEMODE_FALSE) {
                writer.println("online-mode=false");
            } else {
                writer.println("online-mode=true");
            }
            writer.println("allow-flight=false");

            /** - Release file resources */
            writer.close();
        } catch (IOException e) {
            System.out.println("Failed to create LAN server properties: ");
            e.printStackTrace();
        }
    }

    private void editLocalServerPropertiesFile(File serverPropertiesFile) {
        File tempServerPropertiesFile = new File(Minecraft.getRunDirectory(), "_server.properties");
        try {
            Files.copy(serverPropertiesFile.toPath(), tempServerPropertiesFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            FileWriter writer = new FileWriter(tempServerPropertiesFile);
            Reader reader = new FileReader(serverPropertiesFile);
            BufferedReader br = new BufferedReader(reader);

            /** - Scan file for values to change */
            while (br.ready()) {
                String currentLine = br.readLine();

                /** - Change level to currently selected client world */
                if (currentLine.contains("level-name")) {
                    if (null != ModHelper.ModHelperFields.CurrentWorldFolder) {
                        writer.write("level-name=./saves/" + ModHelper.ModHelperFields.CurrentWorldFolder + "\n");
                    }
                }

                /** - Change port to port from GCAPI3 config */
                if (currentLine.contains("server-port")) {
                    if (null != ModHelper.ModHelperFields.CurrentWorldFolder) {
                        writer.write("server-port=" + Config.config.SERVER_PORT + "\n");
                    }
                }

                /** - Change to offline mode if online-mode is forced false */
                if (Config.config.FORCE_ONLINEMODE_FALSE) {
                    if (currentLine.contains("online-mode")) {
                        if (null != ModHelper.ModHelperFields.CurrentWorldFolder) {
                            writer.write("online-mode=false\n");
                        }
                    }
                }
            }

            /** - Release file resources */
            writer.close();
            br.close();
            reader.close();

            /** - Replace current properties file with newly created properties file */
            tempServerPropertiesFile.renameTo(serverPropertiesFile);
        } catch (IOException e) {
            System.out.println("Failed to update LAN server properties: ");
            e.printStackTrace();
        }

        /** - Ensure temporary properties file is deleted if it is no longer needed */
        if (serverPropertiesFile.exists() && tempServerPropertiesFile.exists()) {
            tempServerPropertiesFile.delete();
        }
    }

    public void zipIt(String zipFile) {
        File savesDir = new File(Minecraft.getRunDirectory(), "saves");
        File worldDir = new File(savesDir, ModHelper.ModHelperFields.CurrentWorldFolder);
        byte[] buffer = new byte[1024];
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);

            System.out.println("Output World to Zip : " + zipFile);
            FileInputStream in = null;

            int fileListLength = (null != fileList) ? fileList.size() : 0;
            for (int fileIndex = 0; fileIndex < fileListLength; fileIndex++) {
                System.out.println("Zipping : " + fileList.get(fileIndex));
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
            ex.printStackTrace();
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void generateFileList(File node) {
        // add file only
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.getAbsolutePath().replaceAll("\\\\", "/")));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            assert subNote != null;
            for (String filename : subNote) {
                generateFileList(new File(node, filename));
            }
        }
    }

    private String generateZipEntry(String file) {
        File savesDir = new File(Minecraft.getRunDirectory(), "saves");
        File worldDir = new File(savesDir, ModHelper.ModHelperFields.CurrentWorldFolder);
        return file.substring(worldDir.getAbsolutePath().replaceAll("\\\\", "/").length() + 1, file.length());
    }
}
