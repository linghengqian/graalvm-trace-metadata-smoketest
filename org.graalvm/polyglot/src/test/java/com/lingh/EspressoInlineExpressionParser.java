package com.lingh;

import groovy.lang.Closure;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * Espresso Inline expression parser.
 */
public class EspressoInlineExpressionParser {
    private static final Context polyglot;
    private final Value espressoInlineExpressionParser;

    static {
        // https://github.com/oracle/graal/issues/4555 not yet closed
        String JAVA_HOME = System.getenv("GRAALVM_HOME");
        if (JAVA_HOME == null) {
            JAVA_HOME = System.getenv("JAVA_HOME");
        }
        if (JAVA_HOME == null) {
            throw new RuntimeException("Failed to determine the system's environment variable GRAALVM_HOME or JAVA_HOME!");
        }
        System.setProperty("org.graalvm.home", JAVA_HOME);
        URL resource = Thread.currentThread().getContextClassLoader().getResource("espresso-need-libs");
        assert resource != null;
        String dir = resource.getPath();
        String java_Classpath = String.join(":", dir + "/groovy-4.0.8.jar",
                dir + "/guava-31.1-jre.jar",
                dir + "/shardingsphere-infra-util-5.3.1.jar");
        polyglot = Context.newBuilder().allowAllAccess(true)
                .option("java.MultiThreaded", "true")
                .option("java.Classpath", java_Classpath)
                .build();
    }

    public EspressoInlineExpressionParser(String inlineExpression) {
        espressoInlineExpressionParser = polyglot.getBindings("java")
                .getMember("org.apache.shardingsphere.infra.util.expr.InlineExpressionParser")
                .newInstance(inlineExpression);
    }

    /**
     * Replace all inline expression placeholders.
     *
     * @param inlineExpression inline expression with {@code $->}
     * @return result inline expression with {@code $}
     */
    public static String handlePlaceHolder(final String inlineExpression) {
        return polyglot.getBindings("java")
                .getMember("org.apache.shardingsphere.infra.util.expr.InlineExpressionParser")
                .invokeMember("handlePlaceHolder", inlineExpression)
                .as(String.class);
    }

    /**
     * Split and evaluate inline expression.
     *
     * @return result list
     */
    @SuppressWarnings("unchecked")
    public List<String> splitAndEvaluate() {
        List<String> splitAndEvaluate = espressoInlineExpressionParser.invokeMember("splitAndEvaluate").as(List.class);
        // GraalVM Truffle Espresso CE 22.3.1 has a different behavior for generic arrays than Hotspot.
        return splitAndEvaluate.size() == 0 ? Collections.emptyList() : splitAndEvaluate;
    }

    /**
     * Evaluate closure.
     * TODO
     * This method needs to avoid returning a groovy.lang.Closure class instance,
     * and instead return the result of `Closure#call`.
     * Because `org.graalvm.polyglot.Value#as` does not allow this type to be returned from the guest JVM.
     *
     * @return closure
     */
    public Closure<?> evaluateClosure() {
        return espressoInlineExpressionParser.invokeMember("evaluateClosure").as(Closure.class);
    }
}
