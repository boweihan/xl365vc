package com.xl365vc.api.exception;

public class FileStorageException extends RuntimeException {

	private static final long serialVersionUID = -7821486522604490065L;

	public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}