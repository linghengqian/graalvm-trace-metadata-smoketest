
package com.github.benmanes.caffeine.testing;

import com.google.errorprone.annotations.Immutable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkArgument;

@Immutable
public final class Int implements Serializable {
    public static final Int MAX_VALUE = Int.valueOf(Integer.MAX_VALUE);

    private static final long serialVersionUID = 1L;

    private static final int low = -2048;
    private static final int high = 2048;
    private static final Int[] cache = makeSharedCache();

    private final int value;

    public Int(Int value) {
        this(value.value);
    }

    public Int(int value) {
        this.value = value;
    }


    public int intValue() {
        return value;
    }


    public Int negate() {
        return valueOf(-value);
    }


    public Int add(int i) {
        return valueOf(value + i);
    }


    public Int add(Int i) {
        return add(i.value);
    }


    public CompletableFuture<Int> asFuture() {
        return CompletableFuture.completedFuture(this);
    }

    @Override
    public boolean equals(Object o) {
        return (o == this) || ((o instanceof Int) && (value == ((Int) o).value));
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }


    public static Int valueOf(int i) {
        return ((i >= low) && (i <= high)) ? cache[i - low] : new Int(i);
    }

    public static List<Int> listOf(int... values) {
        switch (values.length) {
            case 1 -> {
                return List.of(valueOf(values[0]));
            }
            case 2 -> {
                return List.of(valueOf(values[0]), valueOf(values[1]));
            }
            case 3 -> {
                return List.of(valueOf(values[0]), valueOf(values[1]), valueOf(values[2]));
            }
            case 4 -> {
                return List.of(valueOf(values[0]), valueOf(values[1]),
                        valueOf(values[2]), valueOf(values[3]));
            }
            default -> {
                var list = new ArrayList<Int>(values.length);
                for (int value : values) {
                    list.add(valueOf(value));
                }
                return list;
            }
        }
    }

    public static Set<Int> setOf(int... values) {
        switch (values.length) {
            case 1 -> {
                return Set.of(valueOf(values[0]));
            }
            case 2 -> {
                return Set.of(valueOf(values[0]), valueOf(values[1]));
            }
            case 3 -> {
                return Set.of(valueOf(values[0]), valueOf(values[1]), valueOf(values[2]));
            }
            case 4 -> {
                return Set.of(valueOf(values[0]), valueOf(values[1]),
                        valueOf(values[2]), valueOf(values[3]));
            }
            default -> {
                var set = new LinkedHashSet<Int>(values.length);
                for (int value : values) {
                    set.add(valueOf(value));
                }
                return set;
            }
        }
    }

    public static Map<Int, Int> mapOf(int... mappings) {
        checkArgument((mappings.length % 2) == 0);
        var map = new LinkedHashMap<Int, Int>(mappings.length / 2);
        for (int i = 0; i < mappings.length; i += 2) {
            map.put(valueOf(mappings[i]), valueOf(mappings[i + 1]));
        }
        return map;
    }


    public static CompletableFuture<Int> futureOf(int i) {
        return valueOf(i).asFuture();
    }


    private static Int[] makeSharedCache() {
        var array = new Int[high - low + 1];
        for (int i = 0; i < array.length; i++) {
            array[i] = new Int(i + low);
        }
        return array;
    }
}
