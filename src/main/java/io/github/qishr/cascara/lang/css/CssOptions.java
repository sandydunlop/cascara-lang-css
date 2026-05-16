package io.github.qishr.cascara.lang.css;

import io.github.qishr.cascara.common.lang.LanguageOptions;

public class CssOptions extends LanguageOptions<CssOptions> {
    private boolean strict = false;

    public CssOptions setStrict(boolean val) {
        this.strict = val;
        return this;
    }

    public boolean isStrict() { return strict; }
}