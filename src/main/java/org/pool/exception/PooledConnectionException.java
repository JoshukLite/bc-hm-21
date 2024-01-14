package org.pool.exception;

public class PooledConnectionException extends RuntimeException {
    public PooledConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
