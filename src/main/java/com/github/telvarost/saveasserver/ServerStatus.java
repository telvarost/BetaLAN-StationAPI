package com.github.telvarost.saveasserver;

public enum ServerStatus {
    NOT_STARTED, // Server has not been started
    INITIALIZING, // Server has been requested to start
    BACKUP, // Server is backing up files
    LAUNCHING,
    PREPARING,
    LOADING,
    STARTED,
    RUNNING


}
