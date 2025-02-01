package com.github.telvarost.betalan;

public enum ServerStatus {
    /**
     * Server has not been started yet
     */
    NOT_STARTED,
    
    /**
     * Determines whether a backup will be done or no
     */
    INITIALIZING,

    /**
     * Backing up the world
     */
    BACKUP,

    /**
     * Launching the server process
     */
    LAUNCHING,

    /**
     * Waiting for the server to load
     */
    LOADING,

    /**
     * Loading is finished, close unnecessary streams and join the server
     */
    STARTED,

    /**
     * The server is running, keep processing messages from it and check if its alive
     */
    RUNNING


}
