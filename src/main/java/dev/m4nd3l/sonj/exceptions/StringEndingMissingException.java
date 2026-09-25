package dev.m4nd3l.sonj.exceptions;

public class StringEndingMissingException extends RuntimeException {
    public StringEndingMissingException() { super("Didn't find an end to the string"); }
    public StringEndingMissingException(String message) { super(message); }
    public StringEndingMissingException(String string, int startPosition)
        { this("Missing '\"' at the end of the string starting at "+ startPosition + " \" " + string); }
    public StringEndingMissingException(String message, Throwable cause) { super(message, cause); }
    public StringEndingMissingException(Throwable cause) { super(cause); }
    protected StringEndingMissingException(String message, Throwable cause,
                                         boolean enableSuppression,
                                         boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
