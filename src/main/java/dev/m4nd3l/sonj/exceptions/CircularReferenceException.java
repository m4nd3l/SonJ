package dev.m4nd3l.sonj.exceptions;

public class CircularReferenceException extends Exception {
    public CircularReferenceException() { super("Circular reference detected!"); }
    public CircularReferenceException(String message) { super(message); }
    public CircularReferenceException(Object obj) { this("Circular reference detected at " + obj.getClass().getSimpleName()); }
    public CircularReferenceException(String message, Throwable cause) { super(message, cause); }
    public CircularReferenceException(Throwable cause) { super(cause); }
    protected CircularReferenceException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
