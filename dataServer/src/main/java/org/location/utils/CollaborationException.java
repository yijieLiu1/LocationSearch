package org.location.utils;

public class CollaborationException extends Exception {
    public CollaborationException(String message) {
        super("Error occurred in cloud collaboration: " + message);
    }
}
