package io.github.qishr.cascara.lang.css.processor;

import io.github.qishr.cascara.common.diagnostic.Reporter;
import io.github.qishr.cascara.common.lang.processor.Parser;
import io.github.qishr.cascara.lang.css.CssDocument;
import io.github.qishr.cascara.lang.css.ast.AtRuleNode;
import io.github.qishr.cascara.lang.css.ast.CssNode;
import io.github.qishr.cascara.lang.css.ast.DeclarationNode;
import io.github.qishr.cascara.lang.css.ast.RuleNode;
import io.github.qishr.cascara.lang.css.ast.StylesheetNode;
import io.github.qishr.cascara.lang.css.token.CssToken;
import io.github.qishr.cascara.lang.css.token.CssTokenType;

import java.net.URI;
import java.util.List;

public class CssParser extends AbstractCssProcessor<CssParser> implements Parser<CssDocument, CssToken> {

    private URI uri;
    private List<CssToken> tokens;
    private int current = 0;

    public CssParser() {
    }

    @Override protected CssParser self() { return this; }

    public CssDocument parse(String text) {
        return parse(text, null);
    }

    public CssDocument parse(String text, URI uri) {
        CssTokenizer tokenizer = new CssTokenizer();
        tokens = tokenizer.tokenize(text);
        return parse(tokens, uri);
    }

    public CssDocument parse(List<CssToken> tokens) {
        return parse(tokens, null);
    }

    public CssDocument parse(List<CssToken> tokens, URI uri) {
        this.tokens = tokens;
        CssToken startToken = peek();
        StylesheetNode stylesheet = new StylesheetNode(startToken);

        while (!isAtEnd()) {
            CssNode topLevelNode = parseTopLevelStatement();
            if (topLevelNode != null) {
                stylesheet.addChild(topLevelNode);
            }
        }

        return new CssDocument(stylesheet);
    }

    // -------------------------------------------------------------------------
    // TOP-LEVEL PARSING
    // -------------------------------------------------------------------------

    private CssNode parseTopLevelStatement() {
        trace("parseTopLevelStatement");

        // Comments can appear anywhere, but we consume them here to keep them in the AST
        while (check(CssTokenType.COMMENT)) {
            advance(); // Ignore for now, but we could create a CommentNode if needed
            // For now, let's just discard comments to simplify the initial AST
        }

        // Handle At-Rules
        if (check(CssTokenType.AT_RULE_NAME)) {
            return parseAtRule();
        }

        // Handle Style Rules (Selectors)
        if (check(CssTokenType.SELECTOR)) {
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
        CssToken atRuleName = consume(CssTokenType.AT_RULE_NAME, "Expected at-rule name (e.g., @media)");

        AtRuleNode atRule = new AtRuleNode(atRuleName);
        atRule.setNameToken(atRuleName);

        // Consume Parameters
        if (check(CssTokenType.AT_RULE_PARAMETER)) {
            CssToken paramToken = advance();
            atRule.setParameterToken(paramToken);
        }

        // Consume Opening Brace '{'
        consume(CssTokenType.DELIMITER, "Expected '{' to start at-rule block");

        // Parse Rule Contents (if it's a block-type at-rule like @media)
        parseBlock(atRule);

        return atRule;
    }

    private CssNode parseRule() {
        trace("parseRule");
        CssToken selector = consume(CssTokenType.SELECTOR, "Expected selector to start rule");

        RuleNode rule = new RuleNode(selector);
        rule.setSelectorToken(selector);

        // Consume Opening Brace '{'
        consume(CssTokenType.DELIMITER, "Expected '{' to start rule block");

        // Parse Declarations
        parseBlock(rule);

        return rule;
    }

    // -------------------------------------------------------------------------
    // BLOCK PARSING (For Rules and At-Rules)
    // -------------------------------------------------------------------------

    private void parseBlock(CssNode parent) {
        trace("parseBlock");

        while (!check(CssTokenType.DELIMITER) && !isAtEnd()) { // Loop until we hit '}' or EOF

            // Handle Declaration inside Rule
            if (check(CssTokenType.PROPERTY_NAME)) {
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
        consume(CssTokenType.DELIMITER, "Expected '}' to close block");
    }


    private DeclarationNode parseDeclaration() {
        trace("parseDeclaration");
        CssToken propertyToken = consume(CssTokenType.PROPERTY_NAME, "Expected property name");

        DeclarationNode declaration = new DeclarationNode(propertyToken);
        declaration.setPropertyToken(propertyToken);

        // Consume Colon ':'
        consume(CssTokenType.DELIMITER, "Expected ':' after property name");

        // Parse Value(s)
        parseDeclarationValue(declaration);

        // Consume Semicolon ';' (Optional but good practice)
        if (check(CssTokenType.DELIMITER)) {
            advance(); // Consume ';'
        }
        // NOTE: The parser allows a declaration without a final semicolon if the next token is '}'

        return declaration;
    }

    private void parseDeclarationValue(DeclarationNode parent) {
        trace("parseDeclarationValue");

        // Consume tokens until we hit a semicolon (;) or a closing brace (})
        CssTokenType[] valueTypes = {
                CssTokenType.KEYWORD, CssTokenType.NUMBER, CssTokenType.UNIT_VALUE, CssTokenType.COLOR_HEX,
                CssTokenType.FUNCTION, CssTokenType.STRING, CssTokenType.OPERATOR, CssTokenType.IMPORTANT,
                CssTokenType.PROPERTY_VALUE_PART // The generic fallback
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
                tokens.get(current).getStartLine(),
                tokens.get(current).getStartColumn(),
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

    private boolean check(CssTokenType... types) {
        if (current >= tokens.size()) return false;
        CssTokenType currentType = peek().getType();
        for (CssTokenType type : types) {
            if (currentType == type) {
                return true;
            }
        }
        return false;
    }

    private boolean isAtEnd() {
        return current >= tokens.size();
    }

    private CssToken consume(CssTokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw new ParseException(message, peek());
    }

    public static class ParseException extends RuntimeException {
        public ParseException(String message, CssToken token) {
            super(String.format("Parser Error: %s at L:%d C:%d. Found: %s",
                                message,
                                token.getStartLine(),
                                token.getStartColumn(),
                                token.getType()));
        }
    }
}
