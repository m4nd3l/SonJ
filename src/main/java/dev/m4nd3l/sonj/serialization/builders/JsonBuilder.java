package dev.m4nd3l.sonj.serialization.builders;

import dev.m4nd3l.sonj.serialization.symbols.BracketType;
import dev.m4nd3l.sonj.serialization.symbols.JsonMainSymbol;
import dev.m4nd3l.sonj.settings.style.Style;

import java.io.IOException;

public class JsonBuilder implements Builder{
    private StringBuilder builder;
    private int indentMultiplier;
    private Style style;

    public JsonBuilder(Style style) {
        this.builder = new StringBuilder();
        this.style = style;
        this.indentMultiplier = style.getIndent().isEmpty() ? -1 : 0;
    }

    @Override
    public Builder newLine() { builder.append(style.getNewLine()); return this; }
    @Override
    public Builder quotation() { return append(JsonMainSymbol.QUOTATION); }

    @Override
    public Builder appendJsonString(String string) { return appendJsonString(string, false); }

    @Override
    public Builder appendJsonString(String string, boolean appendIndent) {
        if (appendIndent) appendIndent();
        quotation();
        append(escape(string));
        quotation();
        return this;
    }

    @Override
    public Builder append(JsonMainSymbol jsonMainSymbol, boolean indent) {
        return appendMainSymbol(jsonMainSymbol.getValue(), jsonMainSymbol.getType(), jsonMainSymbol.getIndentModifier(), indent);
    }

    @Override
    public Builder append(JsonMainSymbol jsonMainSymbol) { return append(jsonMainSymbol, false); }


    @Override
    public Builder append(Object value) {
        if (value instanceof String) value = escape((String) value);
        builder.append(value);
        return this;
    }

    @Override
    public Builder append(Object value, boolean indent) {
        if (value instanceof JsonMainSymbol mainSymbol) return append(mainSymbol, indent);
        if (indent) appendIndent();
        builder.append(value);
        return this;
    }

    @Override
    public Builder appendIndent() {
        if (indentMultiplier == -1) return this;
        builder.append(style.getIndent().repeat(indentMultiplier));
        return this;
    }

    @Override
    public Builder flush() { return this; }

    @Override
    public Builder close() { return this; }

    private Builder appendMainSymbol(String value, BracketType type, int indentModifier, boolean indent) {
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

    @Override
    public String toString() { return builder.toString(); }
}
