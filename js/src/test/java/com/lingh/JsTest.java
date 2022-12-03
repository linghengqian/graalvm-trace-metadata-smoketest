package com.lingh;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class JsTest {
    @SuppressWarnings("resource")
    @Test
    void test() {
        Context context = Context.create();
        Value result = context.eval("js", "40+2");
        assert result.asInt() == 42;
    }
}
