package dev.m4nd3l.sonj.settings.style;

public abstract class Style {
    private boolean spaceAfterColon;
    private String indent;
    private String newLine;

    public String getIndent() { return indent; }
    public String getNewLine() { return newLine; }
    public boolean putSpaceAfterColon() { return spaceAfterColon; }

    public Style setIndent(String indent) { this.indent = indent; return this; }
    public Style setNewLine(String newLine) { this.newLine = newLine; return this; }
    public Style setSpaceAfterColon(boolean spaceAfterColon) { this.spaceAfterColon = spaceAfterColon; return this; }
}
