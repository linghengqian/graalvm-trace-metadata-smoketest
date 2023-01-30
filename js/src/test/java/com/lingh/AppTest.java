package com.lingh;

import org.junit.Assume;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AppTest {
    private static long nashorn;

    @BeforeAll
    public static void nashornBenchmark() throws Exception {
        nashorn = App.benchNashornScriptEngine();
    }

    @Test
    public void testGraalPolyglotSpeed() throws Exception {
        assertGraalVMOrJDK11();
        long graalJS = App.benchGraalPolyglotContext();
        if (nashorn < graalJS) {
            Assertions.fail(String.format("Graal.js (%d ms) should be faster than Nashorn (%d ms).", graalJS, nashorn));
        }
    }

    @Test
    public void testGraalScriptEngineSpeed() throws Exception {
        assertGraalVMOrJDK11();
        long graalJS = App.benchGraalScriptEngine();
        if (nashorn < graalJS) {
            Assertions.fail(String.format("Graal.js (%d ms) should be faster than Nashorn (%d ms).", graalJS, nashorn));
        }
    }

    private void assertGraalVMOrJDK11() {
        if (System.getProperty("java.vm.name").contains("GraalVM")) {
            return;
        }
        try {
            Class.forName("java.lang.Module");
        } catch (ClassNotFoundException ex) {
            Assume.assumeNoException("Skipping the test on regular JDK8", ex);
        }
    }
}
