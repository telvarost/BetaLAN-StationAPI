package com.github.telvarost.betalan;

import com.github.telvarost.betalan.util.FileUtil;
import com.github.telvarost.betalan.util.MathUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.stat.Stats;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings({"deprecation", "DanglingJavadoc", "ResultOfMethodCallIgnored"})
public class LocalServerManager {
    private static LocalServerManager INSTANCE;

    // The thread of the server manager
    public Thread managerThread;

    // The status of the server
    public volatile ServerStatus status;
    public Process serverProcess;
    public BufferedReader serverOutput;
    private long loadingStartTime = 0;

    // Loading Bar
    public volatile String loadingText = "";
    public volatile int loadingProgress = 0;

    // Minecraft.. duh
    public Minecraft minecraft;

    public LocalServerManager() {
        minecraft = (Minecraft) FabricLoader.getInstance().getGameInstance();
        status = ServerStatus.NOT_STARTED;
    }

    /**
     * @return A Singleton instance of the LocalServerManager
     */
    public static LocalServerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LocalServerManager();
        }

        return INSTANCE;
    }

    /**
     * Sets the server status to INITIALIZING and starts the manager thread
     */
    public void start() {
        // Check for the presence of another process
        if (serverProcess != null && serverProcess.isAlive()) {
            BetaLAN.LOGGER.warn("Another server process is running");
            serverProcess.destroy();
        }

        // Check the manager status
        if (status != ServerStatus.NOT_STARTED) {
            BetaLAN.LOGGER.warn("The server manager process status is not NOT_STARTED, this may indicate an improper shutdown");
        }

        // Check if the server manager thread isnt running when it shouldnt
        if (managerThread != null && managerThread.isAlive()) {
            BetaLAN.LOGGER.warn("The server manager thread is running when it should not");
        }

        // Set the correct status
        status = ServerStatus.INITIALIZING;

        // Create the thread that will manage the server
        managerThread = new Thread(new Runnable() {
            final LocalServerManager manager = LocalServerManager.getInstance();

            @Override
            public void run() {
                while (status != ServerStatus.NOT_STARTED) {
                    manager.run();
                }
            }
        });

        // Start the manager thread
        managerThread.start();
    }

    /**
     * Handles the server in its current stage
     */
    public void run() {
        switch (status) {
            // The first stage, this will determine if a backup will be done
            case INITIALIZING -> {
                File savesDir = new File(Minecraft.getRunDirectory(), "saves");
                File worldDir = new File(savesDir, BetaLAN.CurrentWorldFolder);
                BetaLAN.ServerWorld = worldDir;

                if (Config.config.ENABLE_WORLD_SHOWCASE_MODE) {
                    status = ServerStatus.PREPARING;
                } else if (Config.config.BACKUP_WORLD_ON_LAN_SERVER_LAUNCH) {
                    status = ServerStatus.BACKUP;
                } else {
                    status = ServerStatus.LAUNCHING;
                }
            }

            // Backs the server up and launches it
            case BACKUP -> {
                ArrayList<String> fileList = new ArrayList<>();
                File savesDir = new File(Minecraft.getRunDirectory(), "saves");
                File worldDir = new File(savesDir, BetaLAN.CurrentWorldFolder);
                FileUtil.generateFileList(worldDir, fileList);
                FileUtil.zipFiles("saves" + File.separator + "_" + BetaLAN.CurrentWorldFolder + ".zip", fileList);

                BetaLAN.LOGGER.info("World Backup Successful!");
                status = ServerStatus.LAUNCHING;
            }

            // Prepares the showcase world
            case PREPARING -> {
                File showcaseDir = new File(Minecraft.getRunDirectory(), "showcase");
                File showcaseWorldDir = new File(showcaseDir, BetaLAN.CurrentWorldFolder);
                File savesDir = new File(Minecraft.getRunDirectory(), "saves");
                File worldDir = new File(savesDir, BetaLAN.CurrentWorldFolder);

                try {
                    FileUtils.deleteDirectory(showcaseDir);
                    FileUtils.copyDirectory(worldDir, showcaseWorldDir);
                    BetaLAN.ServerWorld = showcaseWorldDir;
                    BetaLAN.LOGGER.info("Showcase World Creation Successful!");
                } catch (IOException e) {
                    BetaLAN.LOGGER.info("Showcase World Creation Failed!");
                }

                status = ServerStatus.LAUNCHING;
            }

            // Starts launching the server
            case LAUNCHING -> {
                /** - Create server lock */
                File serverLock = new File(BetaLAN.ServerWorld, "server.lock");

                if (!serverLock.exists()) {
                    try {
                        serverLock.createNewFile();
                    } catch (IOException e) {
                        BetaLAN.LOGGER.error("Could not create server lock file", e);
                    }
                }

                /** - Update loading bar information */
                loadingText = "Preparing world";
                loadingProgress = 0;

                /** - Launch server */
                String argNoGui = (Config.config.SERVER_GUI_ENABLED) ? "" : "nogui";
                ProcessBuilder processBuilder = new ProcessBuilder(Config.config.JAVA_PATH, "-jar", "local-babric-server.0.16.9.jar", argNoGui);
                processBuilder.directory(Minecraft.getRunDirectory());

                try {
                    // Starts the server process
                    serverProcess = processBuilder.start();

                    // Obtains the input stream and closes the streams we dont need
                    serverOutput = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));
                    serverProcess.getOutputStream().close();
                    serverProcess.getErrorStream().close();

                } catch (IOException ex) {
                    // If the server launch fails, display a message to the user and destroys the process
                    this.minecraft.setScreen(new DisconnectedScreen("Error launching server", ex.getMessage()));
                    BetaLAN.LOGGER.error("Failed to start server!", ex);
                    serverProcess.destroy();
                }

                // Store the time at which the server load started
                loadingStartTime = System.currentTimeMillis();
                status = ServerStatus.LOADING;
            }

            // Reads the messages from the server and either waits for it to load or for the configured timeout
            case LOADING -> {
                // Timeout
                if (System.currentTimeMillis() - loadingStartTime > 180000) {
                    status = ServerStatus.STARTED;
                    BetaLAN.LOGGER.warn("Server didn't start in the specfied timeout, trying to join regardless");
                }

                // Read the messages from the server
                try {
                    if (serverOutput.ready()) {
                        String line = serverOutput.readLine();

                        if (line.charAt(0) == '$') {
                            System.out.println("Processing Line : " + line);

                            String[] splitLine = line.split(";");
                            if (splitLine.length >= 5) {
                                loadingText = splitLine[4];
                                loadingProgress = MathUtil.tryParseInt(splitLine[3], 10);

                                if (splitLine[1].equals("info")) {
                                    if (splitLine[2].equals("done")) {
                                        status = ServerStatus.STARTED;
                                    }
                                } else if (splitLine[1].equals("error")) {
                                    this.minecraft.setScreen(new DisconnectedScreen("Error launching server", splitLine[3]));
                                    this.status = ServerStatus.NOT_STARTED;
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    this.minecraft.setScreen(new DisconnectedScreen("Error launching server", e.getMessage()));
                    this.status = ServerStatus.NOT_STARTED;
                }
            }

            // The server has started, join it and close unnecessary streams
            case STARTED -> {
                // Announce status
                loadingText = "Joining the server";
                loadingProgress = 100;
                BetaLAN.LOGGER.info("Done loading LAN server");

                // Close the streams
                if (!Config.config.enableServerLogsInConsole) {
                    try {
                        serverProcess.getInputStream().close();
                    } catch (IOException ignored) {

                    }
                }

                status = ServerStatus.RUNNING;
            }

            // The server is running
            case RUNNING -> {
                try {
                    // Sleep for 500ms
                    Thread.sleep(500);

                    // Check if the process is still running
                    if (!serverProcess.isAlive()) {
                        BetaLAN.LOGGER.info("Server Process Stopped");
                        status = ServerStatus.NOT_STARTED;
                        break;
                    }

                    // Process log output
                    if (Config.config.enableServerLogsInConsole) {
                        while (serverOutput.ready()) {
                            System.out.println(serverOutput.readLine());
                        }
                    }

                } catch (InterruptedException e) {
                    BetaLAN.LOGGER.error("Interrupted", e);
                } catch (IOException e) {
                    BetaLAN.LOGGER.error("Error reading server console output", e);
                }
            }

            // NOT_STARTED
            default -> {
            }
        }
    }

    public void prepareAndLaunchServer(PlayerEntity player) {
        try {
            /** - Get server files */
            File savesDir = new File(Minecraft.getRunDirectory(), "saves");
            File worldDir = new File(savesDir, BetaLAN.CurrentWorldFolder);
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
                createLocalServerPropertiesFile(serverPropertiesFile, player);
            } else {
                /** - LAN server properties exists, edit LAN server properties file */
                editLocalServerPropertiesFile(serverPropertiesFile, player);
            }

            /** - Extract server jar file */
            File storedServerJar = new File(Minecraft.getRunDirectory().getAbsolutePath() + File.separator + "local-babric-server.0.16.9.jar");
            if (!storedServerJar.exists()) {
                FileUtil.copy(getClass().getResourceAsStream("/assets/betalan/local-babric-server.0.16.9.jar")
                        , Minecraft.getRunDirectory().getAbsolutePath() + File.separator + "local-babric-server.0.16.9.jar");
            }

            /** - Create file telling the server it is a local server */
            File clientLockFile = new File(Minecraft.getRunDirectory(), "client.lock");
            if (!clientLockFile.exists()) {
                clientLockFile.createNewFile();
            }

            /** - Close client world */
            this.minecraft.stats.increment(Stats.LEAVE_GAME, 1);
            if (this.minecraft.isWorldRemote()) {
                this.minecraft.world.disconnect();
            }

            this.minecraft.setWorld(null);
            this.minecraft.setScreen(new OpenToLanScreen(null));
        } catch (Exception ex) {
            BetaLAN.LOGGER.error("Failed to open client world to LAN:", ex);
        }
    }

    private void createLocalServerOpListFile(File serverOpListFile) {
        try {
            PrintWriter writer = new PrintWriter(serverOpListFile, StandardCharsets.UTF_8);
            writer.println(this.minecraft.session.username);

            /** - Release file resources */
            writer.close();
        } catch (IOException e) {
            BetaLAN.LOGGER.error("Failed to create local server op list file", e);
        }
    }

    private void editLocalServerOpListFile(File serverOpListFile) {
        File tempServerOpListFile = new File(Minecraft.getRunDirectory(), "_ops.txt");
        try {
            Files.copy(serverOpListFile.toPath(), tempServerOpListFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            FileWriter writer = new FileWriter(tempServerOpListFile);
            BufferedReader reader = new BufferedReader(new FileReader(tempServerOpListFile));
            boolean isClientPlayerOp = false;

            /** - Scan file for values to change */
            while (reader.ready()) {
                String currentLine = reader.readLine();

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
            reader.close();

            /** - Replace current OP list file with newly created OP list file */
            tempServerOpListFile.renameTo(serverOpListFile);
        } catch (IOException e) {
            BetaLAN.LOGGER.error("Failed to edit local server op list file", e);
        }

        /** - Ensure temporary OP list file is deleted if it is no longer needed */
        if (serverOpListFile.exists() && tempServerOpListFile.exists()) {
            tempServerOpListFile.delete();
        }
    }

    private void createLocalServerPropertiesFile(File serverPropertiesFile, PlayerEntity player) {
        try {
            PrintWriter writer = new PrintWriter(serverPropertiesFile, StandardCharsets.UTF_8);
            writer.println("#Minecraft server properties");
            writer.println("#" + new Date());
            if (null != BetaLAN.CurrentWorldFolder) {
                if (Config.config.ENABLE_WORLD_SHOWCASE_MODE) {
                    writer.println("level-name=./showcase/" + BetaLAN.CurrentWorldFolder);
                } else {
                    writer.println("level-name=./saves/" + BetaLAN.CurrentWorldFolder);
                }
            } else {
                writer.println("level-name=world");
            }
            writer.println("default-gamemode=" + Config.config.ADVANCED_SERVER_CONFIG.DEFAULT_GAMEMODE.ordinal());
            writer.println("view-distance=" + Config.config.ADVANCED_SERVER_CONFIG.VIEW_DISTANCE);
            writer.println("white-list=" + Config.config.ADVANCED_SERVER_CONFIG.ENABLE_WHITELIST);
            writer.println("server-ip=");
            writer.println("pvp=" + Config.config.ADVANCED_SERVER_CONFIG.ENABLE_PVP);
            writer.println("level-seed=");
            writer.println("spawn-animals=" + Config.config.ADVANCED_SERVER_CONFIG.SPAWN_ANIMALS);
            writer.println("server-port=" + Config.config.SERVER_PORT);
            writer.println("allow-nether=" + Config.config.ADVANCED_SERVER_CONFIG.ALLOW_NETHER);
            if (player.world.difficulty >= 1) {
                writer.println("spawn-monsters=true");
            } else {
                writer.println("spawn-monsters=false");
            }
            writer.println("max-players=" + Config.config.ADVANCED_SERVER_CONFIG.MAX_PLAYERS);
            if (Config.config.FORCE_ONLINEMODE_FALSE) {
                writer.println("online-mode=false");
            } else {
                writer.println("online-mode=true");
            }
            writer.println("allow-flight=" + Config.config.ADVANCED_SERVER_CONFIG.ALLOW_FLIGHT);

            /** - Release file resources */
            writer.close();
        } catch (IOException e) {
            BetaLAN.LOGGER.error("Failed to create local server properties file", e);
        }
    }

    private void editLocalServerPropertiesFile(File serverPropertiesFile, PlayerEntity player) {
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
                    if (null != BetaLAN.CurrentWorldFolder) {
                        if (Config.config.ENABLE_WORLD_SHOWCASE_MODE) {
                            writer.write("level-name=./showcase/" + BetaLAN.CurrentWorldFolder + "\n");
                        } else {
                            writer.write("level-name=./saves/" + BetaLAN.CurrentWorldFolder + "\n");
                        }
                    } else {
                        writer.write("level-name=world" + "\n");
                    }
                }

                /** - Advanced config */
                if (currentLine.contains("white-list")) {
                    writer.write("white-list=" + Config.config.ADVANCED_SERVER_CONFIG.ENABLE_WHITELIST + "\n");
                }

                /** - Advanced config */
                if (currentLine.contains("default-gamemode")) {
                    writer.write("default-gamemode=" + Config.config.ADVANCED_SERVER_CONFIG.DEFAULT_GAMEMODE.ordinal() + "\n");
                }

                /** - Advanced config */
                if (currentLine.contains("view-distance")) {
                    writer.write("view-distance=" + Config.config.ADVANCED_SERVER_CONFIG.VIEW_DISTANCE + "\n");
                }

                /** - Advanced config */
                if (currentLine.contains("pvp")) {
                    writer.write("pvp=" + Config.config.ADVANCED_SERVER_CONFIG.ENABLE_PVP + "\n");
                }

                /** - Advanced config */
                if (currentLine.contains("spawn-animals")) {
                    writer.write("spawn-animals=" + Config.config.ADVANCED_SERVER_CONFIG.SPAWN_ANIMALS + "\n");
                }

                /** - Change port to port from GCAPI3 config */
                if (currentLine.contains("server-port")) {
                    writer.write("server-port=" + Config.config.SERVER_PORT + "\n");
                }

                /** - Advanced config */
                if (currentLine.contains("allow-nether")) {
                    writer.write("allow-nether=" + Config.config.ADVANCED_SERVER_CONFIG.ALLOW_NETHER + "\n");
                }

                /** - Change spawn-monsters to match current difficulty setting */
                if (currentLine.contains("spawn-monsters")) {
                    if (player.world.difficulty >= 1) {
                        writer.write("spawn-monsters=true\n");
                    } else {
                        writer.write("spawn-monsters=false\n");
                    }
                }

                /** - Advanced config */
                if (currentLine.contains("max-players")) {
                    writer.write("max-players=" + Config.config.ADVANCED_SERVER_CONFIG.MAX_PLAYERS + "\n");
                }

                /** - Change to offline mode if online-mode is forced false */
                if (Config.config.FORCE_ONLINEMODE_FALSE) {
                    if (currentLine.contains("online-mode")) {
                        writer.write("online-mode=false\n");
                    }
                }

                /** - Advanced config */
                if (currentLine.contains("allow-flight")) {
                    writer.write("allow-flight=" + Config.config.ADVANCED_SERVER_CONFIG.ALLOW_FLIGHT + "\n");
                }
            }

            /** - Release file resources */
            writer.close();
            br.close();
            reader.close();

            /** - Replace current properties file with newly created properties file */
            tempServerPropertiesFile.renameTo(serverPropertiesFile);
        } catch (IOException e) {
            BetaLAN.LOGGER.error("Failed to edit local server properties file", e);
        }

        /** - Ensure temporary properties file is deleted if it is no longer needed */
        if (serverPropertiesFile.exists() && tempServerPropertiesFile.exists()) {
            tempServerPropertiesFile.delete();
        }
    }
}
