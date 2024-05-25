
module com.cyster.adf {
    requires flexmark.util.ast;
    requires flexmark.util.collection;
    requires flexmark.util.format;
    requires flexmark.util.builder;
    requires flexmark.util.data;
    requires flexmark.util.dependency;
    requires flexmark.util.html;
    requires flexmark.util.misc;
    requires flexmark.util.sequence;
    requires flexmark.util.visitor;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    
    exports com.cyster.adf;
}

