package io.github.qishr.cascara.lang.css.ast;

import io.github.qishr.cascara.lang.css.CssToken;

public class StylesheetNode extends CssNode {
    public StylesheetNode(CssToken startToken) {
        setStartToken(startToken);
        setTagName("stylesheet");
    }
}