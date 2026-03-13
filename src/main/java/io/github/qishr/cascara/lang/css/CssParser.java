package io.github.qishr.cascara.lang.css;

import io.github.qishr.cascara.common.diagnostic.Reporter;
import io.github.qishr.cascara.lang.css.CssToken.Type;
import io.github.qishr.cascara.lang.css.ast.AtRuleNode;
import io.github.qishr.cascara.lang.css.ast.CssNode;
import io.github.qishr.cascara.lang.css.ast.DeclarationNode;
import io.github.qishr.cascara.lang.css.ast.RuleNode;
import io.github.qishr.cascara.lang.css.ast.StylesheetNode;

import java.util.List;

public class CssParser {

    private Reporter reporter;
    private List<CssToken> tokens;
    private int current = 0;

    public CssParser() {
    }

    public void setReporter(Reporter reporter){
        this.reporter = reporter;
    }

    /**
     * The main entry point for the parser.
     */
    public Css parse(String text) {
        trace("parse");
        CssTokenizer tokenizer = new CssTokenizer();
        tokens = tokenizer.tokenize(text);

        CssToken startToken = peek();
        StylesheetNode stylesheet = new StylesheetNode(startToken);

        while (!isAtEnd()) {
            CssNode topLevelNode = parseTopLevelStatement();
            if (topLevelNode != null) {
                stylesheet.addChild(topLevelNode);
            }
        }

        return new Css(stylesheet);
    }

    // -------------------------------------------------------------------------
    // TOP-LEVEL PARSING
    // -------------------------------------------------------------------------

    private CssNode parseTopLevelStatement() {
        trace("parseTopLevelStatement");

        // Comments can appear anywhere, but we consume them here to keep them in the AST
        while (check(Type.COMMENT)) {
            advance(); // Ignore for now, but we could create a CommentNode if needed
            // For now, let's just discard comments to simplify the initial AST
        }

        // Handle At-Rules
        if (check(Type.AT_RULE_NAME)) {
            return parseAtRule();
        }

        // Handle Style Rules (Selectors)
        if (check(Type.SELECTOR)) {
            return parseRule();
        }

        // Handle EOF/Remaining tokens
        if (isAtEnd()) {
            return null;
        }

        // Synchronization/Error Recovery: Skip token if we don't recognize it
        CssToken errorToken = advance();
        reporter.error("Unexpected token at top level: " + errorToken.getType(), errorToken);
        return null;
    }

    private CssNode parseAtRule() {
        trace("parseAtRule");
        CssToken atRuleName = consume(Type.AT_RULE_NAME, "Expected at-rule name (e.g., @media)");

        AtRuleNode atRule = new AtRuleNode(atRuleName);
        atRule.setNameToken(atRuleName);

        // Consume Parameters
        if (check(Type.AT_RULE_PARAMETER)) {
            CssToken paramToken = advance();
            atRule.setParameterToken(paramToken);
        }

        // Consume Opening Brace '{'
        consume(Type.DELIMITER, "Expected '{' to start at-rule block");

        // Parse Rule Contents (if it's a block-type at-rule like @media)
        parseBlock(atRule);

        return atRule;
    }

    private CssNode parseRule() {
        trace("parseRule");
        CssToken selector = consume(Type.SELECTOR, "Expected selector to start rule");

        RuleNode rule = new RuleNode(selector);
        rule.setSelectorToken(selector);

        // Consume Opening Brace '{'
        consume(Type.DELIMITER, "Expected '{' to start rule block");

        // Parse Declarations
        parseBlock(rule);

        return rule;
    }

    // -------------------------------------------------------------------------
    // BLOCK PARSING (For Rules and At-Rules)
    // -------------------------------------------------------------------------

    private void parseBlock(CssNode parent) {
        trace("parseBlock");

        while (!check(Type.DELIMITER) && !isAtEnd()) { // Loop until we hit '}' or EOF

            // Handle Declaration inside Rule
            if (check(Type.PROPERTY_NAME)) {
                DeclarationNode decl = parseDeclaration();
                parent.addChild(decl);
            }
            // Handle Nested Rules/AtRules inside block (for future @media support)
            else {
                // Synchronization/Error Recovery: Skip unknown tokens within the block
                CssToken errorToken = advance();
                reporter.error("Unexpected token inside block: " + errorToken.getType(), errorToken);
            }
        }

        // Consume Closing Brace '}'
        consume(Type.DELIMITER, "Expected '}' to close block");
    }


    private DeclarationNode parseDeclaration() {
        trace("parseDeclaration");
        CssToken propertyToken = consume(Type.PROPERTY_NAME, "Expected property name");

        DeclarationNode declaration = new DeclarationNode(propertyToken);
        declaration.setPropertyToken(propertyToken);

        // Consume Colon ':'
        consume(Type.DELIMITER, "Expected ':' after property name");

        // Parse Value(s)
        parseDeclarationValue(declaration);

        // Consume Semicolon ';' (Optional but good practice)
        if (check(Type.DELIMITER)) {
            advance(); // Consume ';'
        }
        // NOTE: The parser allows a declaration without a final semicolon if the next token is '}'

        return declaration;
    }

    private void parseDeclarationValue(DeclarationNode parent) {
        trace("parseDeclarationValue");

        // Consume tokens until we hit a semicolon (;) or a closing brace (})
        Type[] valueTypes = {
                Type.KEYWORD, Type.NUMBER, Type.UNIT_VALUE, Type.COLOR_HEX,
                Type.FUNCTION, Type.STRING, Type.OPERATOR, Type.IMPORTANT,
                Type.PROPERTY_VALUE_PART // The generic fallback
        };

        while (check(valueTypes)) {
            // For now, we'll store the raw value tokens as children of the DeclarationNode
            parent.addChild(new ValueNode(advance()));
        }

        // If no value was consumed, we might have an error, but CSS allows empty declarations.
    }

    // Simple node for all value parts
    public static class ValueNode extends CssNode {
        private final CssToken valueToken;
        public ValueNode(CssToken valueToken) {
            setStartToken(valueToken);
            setTagName("value-part");
            this.valueToken = valueToken;
        }
        public CssToken getValueToken() { return valueToken; }
    }

    /// Log the current method name and upcoming tokens
    private void trace(String methodName) {
        reporter.trace("L%d C%d I%d %28s: %s",
                tokens.get(current).getLine(),
                tokens.get(current).getColumn(),
                current, methodName, upcomingTokens());
    }

    // Get next 4 tokens as a string.
    private String upcomingTokens() {
        StringBuilder sb = new StringBuilder();
        int distance = Math.min(tokens.size() - current, 4);
        for (int i = 0; i < distance; i++) {
            CssToken token = tokens.get(current + i);
            sb.append(token.getType());
            sb.append("(");
            sb.append(token.getLexeme().replace("\n", "\\n").replace("\r", "\\r"));
            sb.append(") ");
        }
        return sb.toString();
    }


    // -------------------------------------------------------------------------
    // CORE PARSER ENGINE METHODS (Lookahead & Consumption)
    // -------------------------------------------------------------------------

    private CssToken advance() {
        if (!isAtEnd()) {
            current++;
        }
        return tokens.get(current - 1);
    }

    private CssToken peek() {
        if (current >= tokens.size()) {
            return tokens.get(tokens.size() - 1);
        }
        return tokens.get(current);
    }

    private boolean check(Type... types) {
        if (current >= tokens.size()) return false;
        Type currentType = peek().getType();
        for (Type type : types) {
            if (currentType == type) {
                return true;
            }
        }
        return false;
    }

    private boolean isAtEnd() {
        return current >= tokens.size();
    }

    private CssToken consume(Type type, String message) {
        if (check(type)) {
            return advance();
        }
        throw new ParseException(message, peek());
    }

    public static class ParseException extends RuntimeException {
        public ParseException(String message, CssToken token) {
            super(String.format("Parser Error: %s at L:%d C:%d. Found: %s",
                                message,
                                token.getLine(),
                                token.getColumn(),
                                token.getType()));
        }
    }
}
