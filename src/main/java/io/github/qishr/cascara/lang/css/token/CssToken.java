package io.github.qishr.cascara.lang.css.token;

import io.github.qishr.cascara.common.lang.token.Token;

public class CssToken implements Token {
    private CssTokenType type;
    private String lexeme;
    private Object value;
    private int offset;
    private int line;
    private int column;

    public CssToken(CssTokenType type, String lexeme, int offset, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.offset = offset;
        this.line = line;
        this.column = column;
    }

    @Override
    public CssTokenType getType() {
        return type;
    }

    @Override
    public String getLexeme() {
        return lexeme;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int getStartLine() {
        return line;
    }

    @Override
    public int getStartColumn() {
        return column;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        String displayLexeme = this.lexeme.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\\\"");
        return String.format("[%-20s | '%-15s' | S:%d L:%d C:%d]", this.type, displayLexeme, this.offset, this.line, this.column);
    }
}
