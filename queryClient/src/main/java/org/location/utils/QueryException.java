package org.location.utils;

public class QueryException extends Exception{
    public QueryException(String message) {
        super("Error occurred in query: " + message);
    }
}
