package dev.m4nd3l.sonj.json;

import dev.m4nd3l.sonj.settings.style.Style;

public class JsonBuilder {
    private StringBuilder builder;
    private int indentMultiplier;
    private Style style;

    public JsonBuilder(Style style) {
        this.builder = new StringBuilder();
        this.style = style;
        this.indentMultiplier = style.getIndent().isEmpty() ? -1 : 0;
    }

    public JsonBuilder newLine() { builder.append(style.getNewLine()); return this; }
    public JsonBuilder quotation() { return append(JsonMainSymbol.QUOTATION); }

    public JsonBuilder appendJsonString(String string) { return appendJsonString(string, false); }

    public JsonBuilder appendJsonString(String string, boolean appendIndent) {
        if (appendIndent) appendIndent();
        quotation();
        append(string);
        quotation();
        return this;
    }

    public JsonBuilder append(JsonMainSymbol jsonMainSymbol, boolean indent) {
        return appendMainSymbol(jsonMainSymbol.getValue(), jsonMainSymbol.getType(), jsonMainSymbol.getIndentModifier(), indent);
    }

    public JsonBuilder append(JsonMainSymbol jsonMainSymbol) { return append(jsonMainSymbol, false); }


    public JsonBuilder append(Object value) {
        if (value instanceof String) value = escape((String) value);
        builder.append(value);
        return this;
    }

    public JsonBuilder append(Object value, boolean indent) {
        if (value instanceof JsonMainSymbol mainSymbol) return append(mainSymbol, indent);
        if (indent) appendIndent();
        builder.append(value);
        return this;
    }

    public JsonBuilder appendIndent() {
        if (indentMultiplier == -1) return this;
        builder.append(style.getIndent().repeat(indentMultiplier));
        return this;
    }

    private JsonBuilder appendMainSymbol(String value, BracketType type, int indentModifier, boolean indent) {
        if (type == BracketType.OPEN) {
            append(value, indent);
            if (indentMultiplier != -1) indentMultiplier += indentModifier;
            return this;
        }
        if (indentMultiplier != -1) indentMultiplier += indentModifier;
        append(value, indent);
        if (value.equals(":") && style.putSpaceAfterColon()) builder.append(' ');
        return this;
    }

    private String escape(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @Override
    public String toString() { return builder.toString(); }
}
