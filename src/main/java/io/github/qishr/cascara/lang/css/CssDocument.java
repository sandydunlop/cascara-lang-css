package io.github.qishr.cascara.lang.css;

import java.net.URI;
import java.util.List;

import io.github.qishr.cascara.common.lang.StructuredDocument;
import io.github.qishr.cascara.common.lang.ast.AstNode;
import io.github.qishr.cascara.common.lang.ast.CommentAstNode;
import io.github.qishr.cascara.lang.css.ast.StylesheetNode;

public class CssDocument implements StructuredDocument {
    StylesheetNode rootNode = null;

    public CssDocument(StylesheetNode rootNode) {
        this.rootNode = rootNode;
    }

    public StylesheetNode getRoot() {
        return rootNode;
    }

    @Override
    public int getStartLine() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStartLine'");
    }

    @Override
    public int getStartColumn() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStartColumn'");
    }

    @Override
    public int getEndLine() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEndLine'");
    }

    @Override
    public int getEndColumn() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEndColumn'");
    }

    @Override
    public List<? extends AstNode> getChildren() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getChildren'");
    }

    @Override
    public List<CommentAstNode> getComments() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getComments'");
    }

    @Override
    public URI getOriginUri() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOriginUri'");
    }

    @Override
    public URI getSchemaUri() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSchemaUri'");
    }
}
