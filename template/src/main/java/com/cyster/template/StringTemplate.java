package com.cyster.template;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

public class StringTemplate {
    private final Mustache mustache;

    public StringTemplate(String template) {
        MustacheFactory factory = new DefaultMustacheFactory();
        this.mustache = factory.compile(new StringReader(template), "template");
    }

    public String render(Map<String, Object> parameters) {
        try (StringWriter writer = new StringWriter()) {
            mustache.execute(writer, parameters).flush();
            return writer.toString();
        } catch (Exception exception) {
            throw new RuntimeException("Error rendering template", exception);
        }
    }
    
    public String render(Object parameters) {
        try (StringWriter writer = new StringWriter()) {
            mustache.execute(writer, parameters).flush();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error rendering template", e);
        }
    }
}

