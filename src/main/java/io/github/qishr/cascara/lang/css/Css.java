package io.github.qishr.cascara.lang.css;

import io.github.qishr.cascara.lang.css.ast.StylesheetNode;

public class Css {
    StylesheetNode rootNode = null;

    public Css(StylesheetNode rootNode) {
        this.rootNode = rootNode;
    }

    public StylesheetNode getRoot() {
        return rootNode;
    }
}
