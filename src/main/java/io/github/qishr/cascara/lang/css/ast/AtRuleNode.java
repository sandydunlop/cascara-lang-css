package io.github.qishr.cascara.lang.css.ast;

import io.github.qishr.cascara.lang.css.CssToken;

public class AtRuleNode extends CssNode {
    private CssToken nameToken;
    private CssToken parameterToken;

    public AtRuleNode(CssToken startToken) {
        setStartToken(startToken);
        setTagName("at-rule");
    }

    public CssToken getNameToken() { return nameToken; }
    public void setNameToken(CssToken nameToken) { this.nameToken = nameToken; }

    public CssToken getParameterToken() { return parameterToken; }
    public void setParameterToken(CssToken parameterToken) { this.parameterToken = parameterToken; }
}