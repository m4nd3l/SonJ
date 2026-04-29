package dev.m4nd3l.sonj.serialization.builders;

import dev.m4nd3l.sonj.serialization.symbols.BracketType;
import dev.m4nd3l.sonj.serialization.symbols.JsonMainSymbol;
import dev.m4nd3l.sonj.settings.style.Style;

import java.io.*;
import java.nio.file.Files;

public class JsonFileBuilder implements Builder {
    File outputFile;
    Writer writer;
    private int indentMultiplier;
    private Style style;

    public JsonFileBuilder(Style style, File outputFile) throws IOException {
        this.style = style;
        this.indentMultiplier = style.getIndent().isEmpty() ? -1 : 0;
        this.outputFile = outputFile;
        this.writer = new BufferedWriter(new FileWriter(outputFile));;

        if (outputFile.exists()) outputFile.createNewFile();
    }

    @Override
    public Builder newLine() throws IOException {
        writer.write(style.getNewLine());
        return this;
    }
    @Override
    public Builder quotation() throws IOException { return append(JsonMainSymbol.QUOTATION); }

    @Override
    public Builder appendJsonString(String string) throws IOException { return appendJsonString(string, false); }

    @Override
    public Builder appendJsonString(String string, boolean appendIndent) throws IOException {
        if (appendIndent) appendIndent();
        quotation();
        append(escape(string));
        quotation();
        return this;
    }

    @Override
    public Builder append(JsonMainSymbol jsonMainSymbol, boolean indent) throws IOException {
        return appendMainSymbol(jsonMainSymbol.getValue(), jsonMainSymbol.getType(), jsonMainSymbol.getIndentModifier(), indent);
    }

    @Override
    public Builder append(JsonMainSymbol jsonMainSymbol) throws IOException { return append(jsonMainSymbol, false); }


    @Override
    public Builder append(Object value) throws IOException {
        if (value instanceof String) value = escape((String) value);
        writer.write(String.valueOf(value));
        return this;
    }

    @Override
    public Builder append(Object value, boolean indent) throws IOException {
        if (value instanceof JsonMainSymbol mainSymbol) return append(mainSymbol, indent);
        if (indent) appendIndent();
        writer.write(String.valueOf(value));
        return this;
    }

    @Override
    public Builder appendIndent() throws IOException {
        if (indentMultiplier == -1) return this;
        writer.write(style.getIndent().repeat(indentMultiplier));
        return this;
    }

    @Override
    public Builder flush() throws IOException { writer.flush(); return this; }

    @Override
    public Builder close() throws IOException { writer.close(); return this; }

    @Override
    public String toString() {
        try { return Files.readString(outputFile.toPath()); }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    private Builder appendMainSymbol(String value, BracketType type, int indentModifier, boolean indent) throws IOException {
        if (type == BracketType.OPEN) {
            append(value, indent);
            if (indentMultiplier != -1) indentMultiplier += indentModifier;
            return this;
        }
        if (indentMultiplier != -1) indentMultiplier += indentModifier;
        append(value, indent);
        if (value.equals(":") && style.putSpaceAfterColon()) writer.write(' ');
        return this;
    }
}
