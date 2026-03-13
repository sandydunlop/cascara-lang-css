package io.github.qishr.cascara.lang.css.ast;

import io.github.qishr.cascara.lang.css.CssToken;

public class RuleNode extends CssNode {
    private CssToken selectorToken;

    public RuleNode(CssToken startToken) {
        setStartToken(startToken);
        setTagName("rule");
    }

    public CssToken getSelectorToken() { return selectorToken; }
    public void setSelectorToken(CssToken selectorToken) { this.selectorToken = selectorToken; }
}