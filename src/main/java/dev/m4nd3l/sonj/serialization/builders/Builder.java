package dev.m4nd3l.sonj.serialization.builders;

import dev.m4nd3l.sonj.serialization.symbols.JsonMainSymbol;

import java.io.IOException;

public interface Builder {
    Builder newLine() throws IOException;
    Builder quotation() throws IOException;
    Builder appendJsonString(String string) throws IOException;
    Builder appendJsonString(String string, boolean appendIndent) throws IOException;
    Builder append(JsonMainSymbol jsonMainSymbol, boolean indent) throws IOException;
    Builder append(JsonMainSymbol jsonMainSymbol) throws IOException;
    Builder append(Object value) throws IOException;
    Builder append(Object value, boolean indent) throws IOException;
    Builder appendIndent() throws IOException;
    Builder flush() throws IOException;
    Builder close() throws IOException;
    String toString();

    default String escape(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
