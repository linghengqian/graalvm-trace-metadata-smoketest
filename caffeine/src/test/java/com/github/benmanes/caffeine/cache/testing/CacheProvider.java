
package com.github.benmanes.caffeine.cache.testing;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Policy;
import com.google.common.collect.ImmutableSet;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;


public final class CacheProvider {
    private static final Class<?> BOUNDED_LOCAL_CACHE = classForName("com.github.benmanes.caffeine.cache.BoundedLocalCache");
    private static final ImmutableSet<Class<?>> GUAVA_INCOMPATIBLE = ImmutableSet.of(
            AsyncCache.class, AsyncLoadingCache.class, BOUNDED_LOCAL_CACHE, Policy.Eviction.class,
            Policy.FixedExpiration.class, Policy.VarExpiration.class, Policy.FixedRefresh.class);

    private final Parameter[] parameters;
    private final Method testMethod;

    private CacheProvider(Method testMethod) {
        this.parameters = testMethod.getParameters();
        this.testMethod = testMethod;
    }

    @DataProvider(name = "caches")
    public static Iterator<Object[]> providesCaches(Method testMethod) {
        return new CacheProvider(testMethod).getTestCases();
    }

    private Iterator<Object[]> getTestCases() {
        return scenarios()
                .map(this::asTestCases)
                .filter(params -> params.length > 0)
                .iterator();
    }

    private Stream<CacheContext> scenarios() {
        var cacheSpec = checkNotNull(testMethod.getAnnotation(CacheSpec.class), "@CacheSpec not found");
        var generator = new CacheGenerator(cacheSpec, Options.fromSystemProperties(),
                isLoadingOnly(), isAsyncOnly(), isGuavaCompatible());
        return generator.generate();
    }

    private Object[] asTestCases(CacheContext context) {
        boolean intern = true;
        CacheGenerator.initialize(context);
        var params = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> clazz = parameters[i].getType();
            if (clazz.isInstance(context)) {
                params[i] = context;
                intern = false;
            } else if (clazz.isInstance(context.cache())) {
                params[i] = context.cache();
            } else if (clazz.isInstance(context.asyncCache)) {
                params[i] = context.asyncCache;
            } else if (clazz.isAssignableFrom(Map.class)) {
                params[i] = context.cache().asMap();
            } else if (clazz.isAssignableFrom(BOUNDED_LOCAL_CACHE)) {
                if (!BOUNDED_LOCAL_CACHE.isInstance(context.cache().asMap())) {
                    return new Object[]{};
                }
                params[i] = context.cache().asMap();
            } else if (clazz.isAssignableFrom(Policy.Eviction.class)) {
                params[i] = context.cache().policy().eviction().orElse(null);
            } else if (clazz.isAssignableFrom(Policy.VarExpiration.class)) {
                params[i] = context.cache().policy().expireVariably().orElseThrow();
            } else if (clazz.isAssignableFrom(Policy.FixedRefresh.class)) {
                params[i] = context.cache().policy().refreshAfterWrite().orElseThrow();
            } else if (clazz.isAssignableFrom(Policy.FixedExpiration.class)) {
                if (parameters[i].isAnnotationPresent(ExpireAfterAccess.class)) {
                    params[i] = context.cache().policy().expireAfterAccess().orElseThrow();
                } else if (parameters[i].isAnnotationPresent(ExpireAfterWrite.class)) {
                    params[i] = context.cache().policy().expireAfterWrite().orElseThrow();
                } else {
                    throw new AssertionError("FixedExpiration must have a qualifier annotation");
                }
            }
            if (params[i] == null) {
                checkNotNull(params[i], "Unknown parameter type: %s", clazz);
            }
        }
        if (intern) {
            CacheContext.intern(context);
        }
        return params;
    }

    private boolean isAsyncOnly() {
        return hasParameterOfType(AsyncCache.class);
    }

    private boolean isLoadingOnly() {
        return hasParameterOfType(AsyncLoadingCache.class) || hasParameterOfType(LoadingCache.class);
    }

    private boolean isGuavaCompatible() {
        return Arrays.stream(parameters)
                .noneMatch(parameter -> GUAVA_INCOMPATIBLE.contains(parameter.getType()));
    }

    private boolean hasParameterOfType(Class<?> clazz) {
        return Arrays.stream(parameters).map(Parameter::getType).anyMatch(clazz::isAssignableFrom);
    }

    private static Class<?> classForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }
}
