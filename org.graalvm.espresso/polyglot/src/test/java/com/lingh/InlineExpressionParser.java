package com.lingh;

import groovy.lang.Closure;

import java.util.List;

/**
 * Inline expression parser.
 */
public final class InlineExpressionParser {
    private final EspressoInlineExpressionParser espressoInlineExpressionParser;
    private final HotspotInlineExpressionParser hotspotInlineExpressionParser;
    private static final boolean isSubstrateVM;

    static {
        // workaround for https://github.com/helidon-io/helidon-build-tools/issues/858
        isSubstrateVM = System.getProperty("java.vm.name").equals("Substrate VM");
    }

    public InlineExpressionParser(String inlineExpression) {
        if (isSubstrateVM) {
            this.hotspotInlineExpressionParser = null;
            this.espressoInlineExpressionParser = new EspressoInlineExpressionParser(inlineExpression);
        } else {
            this.hotspotInlineExpressionParser = new HotspotInlineExpressionParser(inlineExpression);
            this.espressoInlineExpressionParser = null;
        }
    }

    /**
     * Replace all inline expression placeholders.
     *
     * @param inlineExpression inline expression with {@code $->}
     * @return result inline expression with {@code $}
     */
    public static String handlePlaceHolder(final String inlineExpression) {
        if (isSubstrateVM) {
            return EspressoInlineExpressionParser.handlePlaceHolder(inlineExpression);
        } else {
            return HotspotInlineExpressionParser.handlePlaceHolder(inlineExpression);
        }
    }

    /**
     * Split and evaluate inline expression.
     *
     * @return result list
     */
    public List<String> splitAndEvaluate() {
        if (isSubstrateVM) {
            assert espressoInlineExpressionParser != null;
            return espressoInlineExpressionParser.splitAndEvaluate();
        } else {
            assert hotspotInlineExpressionParser != null;
            return hotspotInlineExpressionParser.splitAndEvaluate();
        }
    }

    /**
     * Evaluate closure.
     *
     * @return closure
     */
    public Closure<?> evaluateClosure() {
        if (isSubstrateVM) {
            assert espressoInlineExpressionParser != null;
            return espressoInlineExpressionParser.evaluateClosure();
        } else {
            assert hotspotInlineExpressionParser != null;
            return hotspotInlineExpressionParser.evaluateClosure();
        }
    }
}
