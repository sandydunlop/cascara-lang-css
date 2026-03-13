module cascara.lang.css {
    requires transitive cascara.common;

    requires org.w3c.css.sac;

    exports io.github.qishr.cascara.lang.css;
    exports io.github.qishr.cascara.lang.css.ast;

    opens io.github.qishr.cascara.lang.css;
}
