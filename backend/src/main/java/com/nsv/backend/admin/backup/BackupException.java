package com.nsv.backend.admin.backup;

public class BackupException extends RuntimeException {

    public BackupException(String message, Throwable cause) {
        super(message, cause);
    }

    public BackupException(String message) {
        super(message);
    }
}
