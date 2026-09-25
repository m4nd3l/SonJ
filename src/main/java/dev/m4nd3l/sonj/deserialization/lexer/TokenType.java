package dev.m4nd3l.sonj.deserialization.lexer;

public enum TokenType {
    WHITESPACE,

    COMMA,
    COLON,

    STRING,
    INTEGER,
    DECIMAL,
    TRUE(true),
    FALSE(false),
    NULL(null),

    OPEN_BRACKET,
    CLOSED_BRACKET,
    OPEN_CURLY_BRACKET,
    CLOSED_CURLY_BRACKET,

    END_OF_FILE,

    NOT_FOUND; // NOT_FOUND IS FOR PARSING

    private Object value;
    TokenType() {}
    TokenType(Object value) { this.value = value; }
    public Object getValue() { return value; }
}
