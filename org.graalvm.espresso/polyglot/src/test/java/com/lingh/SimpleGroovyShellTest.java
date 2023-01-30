package com.lingh;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;

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
        System.out.println(sqrt2);
        System.out.println(pi);
    }

    @Test
    @SuppressWarnings("resource")
    @Disabled
    void testGroovyShellInEspresso() {
        Context polyglot = Context.newBuilder().allowNativeAccess(true).build();
        Value groovyShell = polyglot.getBindings("java").getMember(GroovyShell.class.getName());
        Script script = groovyShell.invokeMember("parse", "3*5").as(Script.class);
        assert script != null;
    }
}
