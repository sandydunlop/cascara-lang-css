package io.github.qishr.cascara.lang.css.ast;

import io.github.qishr.cascara.lang.css.CssToken;

public class DeclarationNode extends CssNode {
    private CssToken propertyToken;
    // Value members (can be stored as children or a list of Tokens)

    public DeclarationNode(CssToken startToken) {
        setStartToken(startToken);
        setTagName("declaration");
    }

    public CssToken getPropertyToken() { return propertyToken; }
    public void setPropertyToken(CssToken propertyToken) { this.propertyToken = propertyToken; }
}