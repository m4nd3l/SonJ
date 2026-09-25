package dev.m4nd3l.sonj.exceptions;

public class UnexpectedSymbolException extends RuntimeException {
    public UnexpectedSymbolException() { super("Found an unexpected symbol"); }
    public UnexpectedSymbolException(String message) { super(message); }
    public UnexpectedSymbolException(String symbol, int startPosition)
        { this("Unexpected symbol at "+ startPosition + ": '" + symbol + "'"); }
    public UnexpectedSymbolException(int startPosition)
    { this("Unexpected symbol at "+ startPosition); }
    public UnexpectedSymbolException(String message, Throwable cause) { super(message, cause); }
    public UnexpectedSymbolException(Throwable cause) { super(cause); }
    protected UnexpectedSymbolException(String message, Throwable cause,
                                        boolean enableSuppression,
                                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
