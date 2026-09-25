package dev.m4nd3l.sonj.exceptions;

public class UnknownJsonTypeException extends RuntimeException {
    public UnknownJsonTypeException() { super("Found an unknown keyword in json code"); }
    public UnknownJsonTypeException(String message) { super(message); }
    public UnknownJsonTypeException(String keyword, int startPosition)
        { this("Unknown json keyword at "+ startPosition + ": '" + keyword + "'"); }
    public UnknownJsonTypeException(int startPosition)
    { this("Unknown json token at "+ startPosition); }
    public UnknownJsonTypeException(String message, Throwable cause) { super(message, cause); }
    public UnknownJsonTypeException(Throwable cause) { super(cause); }
    protected UnknownJsonTypeException(String message, Throwable cause,
                                       boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
