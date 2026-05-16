package io.github.qishr.cascara.lang.css.token;

import io.github.qishr.cascara.common.lang.token.TokenCategory;
import io.github.qishr.cascara.common.lang.token.TokenType;

public enum CssTokenType implements TokenType {
    PROPERTY_NAME(TokenCategory.IDENTIFIER),
    PROPERTY_VALUE_PART(TokenCategory.PARAMETER_NAME),
    SELECTOR(TokenCategory.TYPE_NAME),
    DELIMITER(TokenCategory.DELIMITER),
    AT_RULE_NAME(TokenCategory.FIELD_NAME),      // e.g., media, font-face, keyframes
    AT_RULE_PARAMETER(TokenCategory.PARAMETER_NAME), // e.g., screen and (min-width: 600px)
    KEYWORD(TokenCategory.KEYWORD),
    NUMBER(TokenCategory.NUMBER),
    UNIT_VALUE(TokenCategory.NUMBER),
    COLOR_HEX(TokenCategory.NUMBER),
    FUNCTION(TokenCategory.FUNCTION_NAME),
    STRING(TokenCategory.STRING),
    OPERATOR(TokenCategory.OPERATOR),
    IMPORTANT(TokenCategory.OPERATOR),
    COMMENT(TokenCategory.COMMENT);

    private final TokenCategory category;

    CssTokenType(TokenCategory category) {
        this.category = category;
    }

    @Override
    public String getId() {
        return name();
    }

    @Override
    public TokenCategory getCategory() {
        return category;
    }

}
