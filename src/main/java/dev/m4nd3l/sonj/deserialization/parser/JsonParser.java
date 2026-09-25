package dev.m4nd3l.sonj.deserialization.parser;

import dev.m4nd3l.sonj.deserialization.lexer.*;
import dev.m4nd3l.sonj.deserialization.parser.ast.*;

import java.util.*;

public class JsonParser {
    private List<Token> tokens;
    private JsonElement source;
    private int position = 0;

    public JsonParser(List<Token> tokens) {
        this.tokens = tokens.stream()
                .filter(t -> t.type() != TokenType.WHITESPACE)
                .toList();
    }

    public JsonElement parse() {
        source = parseValue();
        if (position < tokens.size()) { throw new RuntimeException("Trailing data found after JSON root"); }
        return source;
    }

    private JsonElement parseValue() {
        Token token = peek();
        return switch (token.type()) {
            case OPEN_CURLY_BRACKET -> parseObject();
            case OPEN_BRACKET       -> parseArray();
            case STRING, INTEGER, DECIMAL, TRUE, FALSE, NULL -> {
                Token valueToken = consume();
                yield new JsonValue(valueToken.type(), valueToken.value());
            }
            default -> throw new RuntimeException("Unexpected token: " + token.type());
        };
    }

    private JsonObject parseObject() {
        JsonObject jsonObject = new JsonObject();
        match(TokenType.OPEN_CURLY_BRACKET);

        if (peek().type() == TokenType.CLOSED_CURLY_BRACKET) {
            consume();
            return jsonObject;
        }

        while (true) {
            Token keyToken = consume();
            if (keyToken.type() != TokenType.STRING) throw new RuntimeException("Key must be a string");

            match(TokenType.COLON);
            jsonObject.put((String) keyToken.value(), parseValue());

            if (peek().type() == TokenType.CLOSED_CURLY_BRACKET) {
                consume();
                break;
            }
            match(TokenType.COMMA);
        }
        return jsonObject;
    }

    private JsonArray parseArray() {
        JsonArray jsonArray = new JsonArray();
        match(TokenType.OPEN_BRACKET);

        if (peek().type() == TokenType.CLOSED_BRACKET) {
            consume();
            return jsonArray;
        }

        while (true) {
            jsonArray.put(parseValue());

            if (peek().type() == TokenType.CLOSED_BRACKET) {
                consume();
                break;
            }
            match(TokenType.COMMA);
        }
        return jsonArray;
    }

    private Token consume() { return tokens.get(position++); }

    private Token peek() {
        if (tokens.size() > position) return tokens.get(position);
        return new Token("EOF", null, TokenType.END_OF_FILE, -1);
    }

    private void match(TokenType expected) {
        if (peek().type() != expected) { throw new RuntimeException("Expected " + expected + " but found " + peek().type()); }
        consume();
    }

    public JsonElement getSource() { return source; }
}