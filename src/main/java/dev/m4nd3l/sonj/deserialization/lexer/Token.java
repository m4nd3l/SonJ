package dev.m4nd3l.sonj.deserialization.lexer;

public record Token(String relatedCode, Object value, TokenType type, int position) { }
