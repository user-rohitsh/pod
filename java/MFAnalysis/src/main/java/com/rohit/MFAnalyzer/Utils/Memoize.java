package com.rohit.MFAnalyzer.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Memoize {

    public static <T, R> Function<T, R> memoize(Function<T, R> f) {
        return new Function<T, R>() {
            private final Map<T, R> memory = new HashMap<>();

            @Override
            public R apply(T t) {
                return memory.computeIfAbsent(t, f::apply);
            }
        };
    }

    public static <T1, T2, R> BiFunction<T1, T2, R> memoize(BiFunction<T1, T2, R> f) {
        return new BiFunction<T1, T2, R>() {

            private final Function<T1, Function<T2, R>> curried = t1 -> t2 -> f.apply(t1, t2);
            private final Map<T1, Function<T2, R>> memory = new HashMap<>();

            @Override
            public R apply(T1 t1, T2 t2) {
                Function<T2, R> func = memory.computeIfAbsent(t1, t -> memoize(curried.apply(t)));
                return func.apply(t2);
            }
        };
    }
}
