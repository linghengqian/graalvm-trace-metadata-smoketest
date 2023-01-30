package com.lingh.other;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class EspressoTest {
    @Test
    @Disabled
    void testMain() {
        Context ctx = Context.newBuilder("java")
                .option("java.Polyglot", "true")
                .allowAllAccess(true)
                .build();
        compareDateTimeHandling(ctx);
        compareDateHandling(ctx);
    }

    private static void compareDateTimeHandling(Context ctx) {
        Value fooHost = ctx.asValue(Foo.class);
        Value fooEspresso = ctx.getBindings("java").getMember("com.lingh.other.Foo");
        System.out.println("Running Host:");
        runZDTTest(fooHost);
        System.out.println("Running Espresso:");
        try {
            runZDTTest(fooEspresso);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void compareDateHandling(Context ctx) {
        Value fooHost = ctx.asValue(Foo.class);
        Value fooEspresso = ctx.getBindings("java").getMember("com.lingh.other.Foo");
        System.out.println("Running Host:");
        runLDTest(fooHost);
        System.out.println("Running Espresso:");
        try {
            runLDTest(fooEspresso);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runZDTTest(Value klass) {
        ZoneId tz = ZoneId.of("Europe/Warsaw");
        ZonedDateTime date = ZonedDateTime.of(2022, 9, 22, 12, 0, 0, 0, tz);
        klass.getMember("static").invokeMember("printDateTime", date);
    }


    private static void runLDTest(Value klass) {
        LocalDate date = LocalDate.of(2022, 9, 22);
        klass.getMember("static").invokeMember("printDate", date);
    }
}