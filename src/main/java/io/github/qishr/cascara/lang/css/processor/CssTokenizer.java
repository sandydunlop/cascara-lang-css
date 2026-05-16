package io.github.qishr.cascara.lang.css.processor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.qishr.cascara.common.lang.processor.Tokenizer;
import io.github.qishr.cascara.lang.css.token.CssToken;
import io.github.qishr.cascara.lang.css.token.CssTokenType;

public class CssTokenizer extends AbstractCssProcessor<CssTokenizer> implements Tokenizer<CssToken> {
    private URI uri;
    private String input;
    private int pos = 0;
    private int line = 1;
    private int column = 1;

    // States to help distinguish context
    private enum State { SELECTOR, PROPERTY, VALUE, AT_RULE }
    private State currentState = State.SELECTOR;

    @Override protected CssTokenizer self() { return this; }

    public List<CssToken> tokenize(String text) {
        return tokenize(text, null);
    }

    public List<CssToken> tokenize(String text, URI uri) {
        this.input = text;
        this.pos = 0;
        this.line = 1;
        this.column = 1;
        this.currentState = State.SELECTOR;

        List<CssToken> tokens = new ArrayList<>();

        while (pos < input.length()) {
            char ch = input.charAt(pos);

            // 1. Skip Whitespace
            if (Character.isWhitespace(ch)) {
                advance();
                continue;
            }

            // 2. Handle Comments
            if (ch == '/' && peek() == '*') {
                tokens.add(consumeComment());
                continue;
            }

            // 3. Handle Delimiters and State Changes
            if (ch == '{') {
                tokens.add(createToken(CssTokenType.DELIMITER, "{"));
                // If we were in an At-Rule (like @font-face), we now expect properties.
                // If it was @media, we expect selectors.
                // Defaulting to PROPERTY is often best for IDE syntax highlighting.
                currentState = State.PROPERTY;
                advance();
                continue;
            }
            if (ch == '}') {
                tokens.add(createToken(CssTokenType.DELIMITER, "}"));
                currentState = State.SELECTOR;
                advance();
                continue;
            }
            if (ch == ':') {
                tokens.add(createToken(CssTokenType.OPERATOR, ":"));
                if (currentState == State.PROPERTY) currentState = State.VALUE;
                advance();
                continue;
            }
            if (ch == ';') {
                tokens.add(createToken(CssTokenType.DELIMITER, ";"));
                // If we just finished an @import or a property value, return to the relevant state
                if (currentState == State.VALUE) {
                    currentState = State.PROPERTY;
                } else if (currentState == State.AT_RULE) {
                    currentState = State.SELECTOR;
                }
                advance();
                continue;
            }
            // 4. Handle At-Rules
            if (ch == '@') {
                tokens.add(consumeAtRule(tokens));
                continue;
            }

            // 5. Context-Based Tokenization
            tokens.add(consumeByState());
        }

        return tokens;
    }

    private CssToken consumeByState() {
        return switch (currentState) {
            case SELECTOR -> consumeSelector();
            case PROPERTY -> consumePropertyName();
            case VALUE -> consumeValuePart();
            case AT_RULE -> consumeAtRuleParameter();
        };
    }

    private CssToken consumePropertyName() {
        StringBuilder sb = new StringBuilder();
        int startPos = pos;
        int startLine = line;
        int startCol = column;

        while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '-')) {
            sb.append(input.charAt(pos));
            advance();
        }
        return new CssToken(CssTokenType.PROPERTY_NAME, sb.toString(), startPos, startLine, startCol);
    }

    private CssToken consumeValuePart() {
        // Matches hex colors, units (10px), or strings
        String remaining = input.substring(pos);

        // Hex Color
        if (remaining.startsWith("#")) {
            return consumeRegex(Pattern.compile("^#[a-fA-F0-9]{3,8}"), CssTokenType.COLOR_HEX);
        }
        // Important flag
        if (remaining.startsWith("!important")) {
            return consumeRegex(Pattern.compile("^!important"), CssTokenType.IMPORTANT);
        }
        // Unit/Number
        if (Character.isDigit(remaining.charAt(0)) || remaining.charAt(0) == '.') {
            return consumeRegex(Pattern.compile("^[0-9.]+(%|[a-z]+)?"), CssTokenType.UNIT_VALUE);
        }

        // Default to general value part (like 'sans-serif' or 'bold')
        return consumeRegex(Pattern.compile("^[^;!}]+"), CssTokenType.PROPERTY_VALUE_PART);
    }

    private CssToken consumeSelector() {
        // Collects everything until the next '{'
        return consumeRegex(Pattern.compile("^[^{]+"), CssTokenType.SELECTOR);
    }

    private CssToken consumeComment() {
        int startPos = pos;
        int startLine = line;
        int startCol = column;
        StringBuilder sb = new StringBuilder();

        while (pos < input.length()) {
            sb.append(input.charAt(pos));
            if (input.charAt(pos) == '*' && peek() == '/') {
                advance(); // consume *
                sb.append(input.charAt(pos));
                advance(); // consume /
                break;
            }
            advance();
        }
        return new CssToken(CssTokenType.COMMENT, sb.toString(), startPos, startLine, startCol);
    }

    // Helper to match a pattern at current position and advance
    private CssToken consumeRegex(Pattern pattern, CssTokenType type) {
        Matcher m = pattern.matcher(input.substring(pos));
        if (m.find()) {
            int startPos = pos;
            int startLine = line;
            int startCol = column;
            String lexeme = m.group();
            for (int i = 0; i < lexeme.length(); i++) advance();
            return new CssToken(type, lexeme.trim(), startPos, startLine, startCol);
        }
        // Fallback
        String lexeme = String.valueOf(input.charAt(pos));
        CssToken t = createToken(type, lexeme);
        advance();
        return t;
    }

    private void advance() {
        if (pos < input.length()) {
            if (input.charAt(pos) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            pos++;
        }
    }

    private char peek() {
        return (pos + 1 < input.length()) ? input.charAt(pos + 1) : '\0';
    }

    private CssToken createToken(CssTokenType type, String lexeme) {
        return new CssToken(type, lexeme, pos, line, column);
    }

    private CssToken consumeAtRule(List<CssToken> tokens) {
        int startPos = pos;
        int startLine = line;
        int startCol = column;

        // 1. Consume the '@'
        advance();

        // 2. Consume the identifier (e.g., "media", "import")
        StringBuilder name = new StringBuilder();
        while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos))) {
            name.append(input.charAt(pos));
            advance();
        }

        String atRuleName = name.toString();

        // 3. Set state to handle parameters
        // If it's an rule that usually ends in a semicolon (like @import),
        // it stays in AT_RULE until ';'
        // If it's a block rule (like @media), it stays in AT_RULE until '{'
        currentState = State.AT_RULE;

        return new CssToken(CssTokenType.AT_RULE_NAME, "@" + atRuleName, startPos, startLine, startCol);
    }

    private CssToken consumeAtRuleParameter() {
        // Collect everything until we hit a '{' or a ';'
        // e.g., @media "screen and (max-width: 600px)" {
        StringBuilder sb = new StringBuilder();
        int startPos = pos;
        int startLine = line;
        int startCol = column;

        while (pos < input.length()) {
            char ch = input.charAt(pos);
            if (ch == '{') {
                // State will change to SELECTOR (for nested rules)
                // or PROPERTY in the main loop handler for '{'
                break;
            }
            if (ch == ';') {
                break;
            }
            sb.append(ch);
            advance();
        }

        return new CssToken(CssTokenType.AT_RULE_PARAMETER, sb.toString().trim(), startPos, startLine, startCol);
    }
}