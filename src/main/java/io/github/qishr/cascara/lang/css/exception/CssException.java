package io.github.qishr.cascara.lang.css.exception;

import java.net.URI;

import io.github.qishr.cascara.common.lang.exception.ParserException;

public class CssException extends ParserException {

    public CssException(String message, Throwable cause) {
        super(message, cause, UNKNOWN_COORD, UNKNOWN_COORD, null);
    }

    public CssException(String message, int line, int column, URI uri) {
        super(message, line, column, uri);
    }

    public CssException(String message, Throwable cause, int line, int column, URI uri) {
        super(message, cause, line, column, uri);
    }
}
