package io.github.qishr.cascara.lang.css.ast;

import java.util.ArrayList;
import java.util.List;

import io.github.qishr.cascara.lang.css.CssToken;

public class CssNode {
    CssNode parent = null;
    List<CssNode> children = new ArrayList<>();
    String anchorName = null;
    String tagName = null;
    CssToken startToken;

    public void addChild(CssNode child) {
        if (child != null) {
            child.setParent(this);
            this.children.add(child);
        }
    }

    public void setParent(CssNode parent) {
        this.parent = parent;
    }

    public CssNode getParent() {
        return parent;
    }

    public List<CssNode> getChildren() {
        return children;
    }

    public <T extends CssNode> List<T> getDescendants(Class<T> type) {
        List<T> constructList = new ArrayList<>();
        traverseConstructs(this, type, constructList);
        return constructList;
    }

    private <T extends CssNode> void traverseConstructs(CssNode node, Class<T> type, List<T> constructList) {
        if (type.isInstance(node)) {
            constructList.add(type.cast(node));
        }
        for (CssNode child : node.getChildren()) {
            traverseConstructs(child, type, constructList);
        }
    }

    public <T extends CssNode> T getFirstDescendant(Class<T> type) {
        for (CssNode child : this.getChildren()) {
            if (type.isInstance(child)) {
                return type.cast(child);
            }
            T instance = child.getFirstDescendant(type);
            if (instance != null) {
                return instance;
            }
        }
        return null;
    }

    public <T extends CssNode> T getFirstAncestor(Class<T> type) {
        CssNode node = this.getParent();
        while (node != null) {
            if (type.isInstance(node)) {
                return type.cast(node);
            }
            node = node.getParent();
        }
        return null;
    }

    public String getAnchorName() {
        return anchorName;
    }

    public void setAnchorName(String anchorName) {
        this.anchorName = anchorName;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public CssToken getStartToken() {
        return startToken;
    }

    public void setStartToken(CssToken startToken) {
        this.startToken = startToken;
    }

    // public CssNode findNode(CssNode root, String... path) {
    // }

    // public CssNode findNode(String... path) {
    //     return findNode(this, path);
    // }

    // public String getString(CssNode root, String... path) {
    //     CssNode node = findNode(root, path);
    //     return null;
    // }

    // public String getString(String... path) {
    //     return getString(this, path);
    // }

}
