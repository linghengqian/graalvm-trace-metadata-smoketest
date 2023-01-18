
package com.github.benmanes.caffeine.cache.testing;

import uk.org.lidalia.slf4jext.Level;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Target({METHOD, TYPE})
@Retention(RUNTIME)
public @interface CheckMaxLogLevel {
    Level value();
}
