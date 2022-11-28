package com.lingh;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.transform.CompileStatic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Inline expression parser.
 */
@CompileStatic
public final class InlineExpressionParser {
    
    private static final char SPLITTER = ',';
    
    private static final Map<String, Script> SCRIPTS = new ConcurrentHashMap<>();
    
    private static final GroovyShell SHELL = new GroovyShell();
    
    private final String inlineExpression;

    public InlineExpressionParser(String inlineExpression) {
        this.inlineExpression = inlineExpression;
    }

    /**
     * Replace all inline expression placeholders.
     * 
     * @param inlineExpression inline expression with {@code $->}
     * @return result inline expression with {@code $}
     */
    public static String handlePlaceHolder(final String inlineExpression) {
        return inlineExpression.contains("$->{") ? inlineExpression.replaceAll("\\$->\\{", "\\$\\{") : inlineExpression;
    }
    
    /**
     * Split and evaluate inline expression.
     *
     * @return result list
     */
    public List<String> splitAndEvaluate() {
        return Strings.isNullOrEmpty(inlineExpression) ? Collections.emptyList() : flatten(evaluate(split()));
    }
    
    /**
     * Evaluate closure.
     *
     * @return closure
     */
    public Closure<?> evaluateClosure() {
        return (Closure<?>) evaluate("{it -> \"" + inlineExpression + "\"}");
    }
    
    private List<Object> evaluate(final List<String> inlineExpressions) {
        List<Object> result = new ArrayList<>(inlineExpressions.size());
        for (String each : inlineExpressions) {
            StringBuilder expression = new StringBuilder(handlePlaceHolder(each));
            if (!each.startsWith("\"")) {
                expression.insert(0, "\"");
            }
            if (!each.endsWith("\"")) {
                expression.append("\"");
            }
            result.add(evaluate(expression.toString()));
        }
        return result;
    }
    
    private Object evaluate(final String expression) {
        Script script;
        if (SCRIPTS.containsKey(expression)) {
            script = SCRIPTS.get(expression);
        } else {
            script = SHELL.parse(expression);
            SCRIPTS.put(expression, script);
        }
        return script.run();
    }
    
    private List<String> split() {
        List<String> result = new ArrayList<>();
        StringBuilder segment = new StringBuilder();
        int bracketsDepth = 0;
        for (int i = 0; i < inlineExpression.length(); i++) {
            char each = inlineExpression.charAt(i);
            switch (each) {
                case SPLITTER:
                    if (bracketsDepth > 0) {
                        segment.append(each);
                    } else {
                        result.add(segment.toString().trim());
                        segment.setLength(0);
                    }
                    break;
                case '$':
                    if ('{' == inlineExpression.charAt(i + 1)) {
                        bracketsDepth++;
                    }
                    if ("->{".equals(inlineExpression.substring(i + 1, i + 4))) {
                        bracketsDepth++;
                    }
                    segment.append(each);
                    break;
                case '}':
                    if (bracketsDepth > 0) {
                        bracketsDepth--;
                    }
                    segment.append(each);
                    break;
                default:
                    segment.append(each);
                    break;
            }
        }
        if (segment.length() > 0) {
            result.add(segment.toString().trim());
        }
        return result;
    }
    
    private List<String> flatten(final List<Object> segments) {
        List<String> result = new ArrayList<>();
        for (Object each : segments) {
            if (each instanceof GString) {
                result.addAll(assemblyCartesianSegments((GString) each));
            } else {
                result.add(each.toString());
            }
        }
        return result;
    }
    
    private List<String> assemblyCartesianSegments(final GString segment) {
        Set<List<String>> cartesianValues = getCartesianValues(segment);
        List<String> result = new ArrayList<>(cartesianValues.size());
        for (List<String> each : cartesianValues) {
            result.add(assemblySegment(each, segment));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Set<List<String>> getCartesianValues(final GString segment) {
        List<Set<String>> result = new ArrayList<>(segment.getValues().length);
        for (Object each : segment.getValues()) {
            if (null == each) {
                continue;
            }
            if (each instanceof Collection) {
                result.add(((Collection<Object>) each).stream().map(Object::toString).collect(Collectors.toCollection(LinkedHashSet::new)));
            } else {
                result.add(Sets.newHashSet(each.toString()));
            }
        }
        return Sets.cartesianProduct(result);
    }
    
    private String assemblySegment(final List<String> cartesianValue, final GString segment) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < segment.getStrings().length; i++) {
            result.append(segment.getStrings()[i]);
            if (i < cartesianValue.size()) {
                result.append(cartesianValue.get(i));
            }
        }
        return result.toString();
    }
}