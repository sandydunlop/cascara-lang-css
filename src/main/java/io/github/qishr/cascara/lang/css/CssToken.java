package io.github.qishr.cascara.lang.css;

public class CssToken {
    private Type type;
    private String lexeme;
    private int offset;
    private int line;
    private int column;

    public CssToken(Type type, String lexeme, int offset, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.offset = offset;
        this.line = line;
        this.column = column;
    }

    public Type getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getOffset() {
        return offset;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String toString() {
        String displayLexeme = this.lexeme.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\\\"");
        return String.format("[%-20s | '%-15s' | S:%d L:%d C:%d]", this.type, displayLexeme, this.offset, this.line, this.column);
    }

    public enum Type {
        PROPERTY_NAME,
        PROPERTY_VALUE_PART,
        SELECTOR,
        DELIMITER,
        AT_RULE_NAME,      // e.g., media, font-face, keyframes
        AT_RULE_PARAMETER, // e.g., screen and (min-width: 600px)
        KEYWORD,
        NUMBER,
        UNIT_VALUE,
        COLOR_HEX,
        FUNCTION,
        STRING,
        OPERATOR,
        IMPORTANT,
        COMMENT
    }
}
