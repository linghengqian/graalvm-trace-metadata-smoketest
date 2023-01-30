package com.lingh;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SimpleGroovyShellTest {
    @Test
    @DisabledInNativeImage
    void testGroovyShell() {
        GroovyShell shell = new GroovyShell();
        Script script = shell.parse("3*5");
        assert (Integer) script.run() == 15;
    }

    @Test
    @SuppressWarnings("resource")
    void testJavaInEspresso() {
        Context polyglot = Context.newBuilder().allowNativeAccess(true).build();
        Value java_lang_Math = polyglot.getBindings("java").getMember(Math.class.getName());
        double sqrt2 = java_lang_Math.invokeMember("sqrt", 2).asDouble();
        double pi = java_lang_Math.getMember("PI").asDouble();
        assert sqrt2 == Math.sqrt(2);
        assert pi == Math.PI;
    }

    @Test
    @Disabled
    @DisabledInNativeImage
    void testGroovyShellInEspresso() {
        try (Context polyglot = Context.newBuilder().allowNativeAccess(true).build()) {
            Value inlineExpressionParser = polyglot.getBindings("java").getMember("com.lingh.InlineExpressionParser");
            assert inlineExpressionParser != null;
            assertThat(inlineExpressionParser.invokeMember("handlePlaceHolder", "t_$->{[\"new$->{1+2}\"]}").as(String.class),
                    is("t_${[\"new${1+2}\"]}"));
            assertThat(inlineExpressionParser.invokeMember("handlePlaceHolder", "t_${[\"new$->{1+2}\"]}").as(String.class),
                    is("t_${[\"new${1+2}\"]}"));
        }
    }
}
