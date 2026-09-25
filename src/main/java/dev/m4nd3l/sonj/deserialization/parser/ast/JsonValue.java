package dev.m4nd3l.sonj.deserialization.parser.ast;

import dev.m4nd3l.sonj.deserialization.lexer.TokenType;

public class JsonValue extends JsonElement {
    public static final JsonValue NULL = new JsonValue(TokenType.NULL, null);
    public static final JsonValue NOT_FOUND = new JsonValue(TokenType.NOT_FOUND, null);

    private TokenType type;
    private Object value;
    public JsonValue(TokenType type, Object value) { this.type = type; this.value = value; }
    public TokenType getType() { return type; }
    public Object getValue() { return value; }
}
