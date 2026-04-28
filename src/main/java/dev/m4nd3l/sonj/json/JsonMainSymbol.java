package dev.m4nd3l.sonj.json;

public enum JsonMainSymbol {
    OPEN_CURLY_BRACKET("{", 1, BracketType.OPEN),
    CLOSED_CURLY_BRACKET("}", -1, BracketType.CLOSED),

    OPEN_BRACKET("[", 1, BracketType.OPEN),
    CLOSED_BRACKET("]", -1, BracketType.CLOSED),

    COLON(":"),

    QUOTATION("\""),

    COMMA(","),

    NULL("null");

    private String value;
    private int indentModifier;
    private BracketType type;

    JsonMainSymbol(String value, int indentModifier, BracketType type) {
        this.value = value;
        this.type = type;
        this.indentModifier = indentModifier;
    }

    JsonMainSymbol(String value, int indentModifier) {
        this.value = value;
        this.indentModifier = indentModifier;
    }

    JsonMainSymbol(String value) {
        this.value = value;
        this.indentModifier = 0;
    }

    public int getIndentModifier() { return indentModifier; }
    public String getValue() { return value; }
    public BracketType getType() { return type; }
}
