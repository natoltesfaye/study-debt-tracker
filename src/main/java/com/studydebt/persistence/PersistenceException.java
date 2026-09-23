package com.studydebt.persistence;

/**
 * Wraps low-level IO failures so callers deal with one clear exception
 * type instead of checked IOExceptions leaking through every layer.
 */
public class PersistenceException extends RuntimeException {
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
