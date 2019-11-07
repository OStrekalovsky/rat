package net.ostrekalovsky.rat.service;


public class ReceiptsImportException extends Exception {

    public ReceiptsImportException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReceiptsImportException(String message) {
        super(message);
    }
}
