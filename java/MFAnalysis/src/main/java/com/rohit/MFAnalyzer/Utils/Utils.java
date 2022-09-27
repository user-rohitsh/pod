package com.rohit.MFAnalyzer.Utils;

import io.vavr.CheckedFunction1;
import io.vavr.collection.Array;
import io.vavr.control.Try;
import org.javatuples.Pair;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class Utils {

    public static Try<Stream<String>> getLines(String dir) {

        Try<Stream<Path>> checked_paths = CheckedFunction1.liftTry(
                (String directory) ->
                {
                    Path p = FileSystems.getDefault().getPath(directory);
                    return Files.walk(p);
                }).apply(dir);

        Function<Stream<Path>, Try<Stream<String>>> checked_convert_paths_streams = CheckedFunction1.liftTry((Stream<Path> paths) -> Utils.convertPathsToLines(paths));
        return checked_paths.flatMap(paths -> checked_convert_paths_streams.apply(paths));
    }

    public static Stream<String> convertPathsToLines(Stream<Path> paths) {
        return paths
                .filter(Files::isRegularFile)
                .flatMap(path -> {
                    try {
                        return Files.lines(path, Charset.defaultCharset()).skip(1);
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                });
    }

    public static double annuityDueFV(double rate_percent, int years) {
        if (rate_percent == 0) return 12000.0 * years;
        double rate = rate_percent / 100.0;
        double fv = Math.pow(1 + rate, years) - 1;
        fv = fv / rate;
        fv = fv * 12000.0;
        fv = fv * (1 + rate);
        return Math.round(fv * 100.0) / 100.0;
    }

    //generate an array based on function f in domain [start, end); increment values by incrementer
    public static <T extends Comparable<T>, R> Pair<T, R>[] ArrayFromFunction(Function<T, R> f, T start, T end, UnaryOperator<T> incrementer) {
        ArrayList<Pair<T, R>> ret = new ArrayList<>();

        T temp = start;
        while (temp.compareTo(end) < 0) {
            ret.add(Pair.with(temp, f.apply(temp)));
            temp = incrementer.apply(temp);
        }

        return ret.toArray(new Pair[ret.size()]);
    }

    //first index with value greater than or equal to val
    public static <T extends Comparable<T>, R extends Comparable<R>>
    T lowerBound(Pair<T, R>[] arr, R val) {

        T ret_val = null;
        for (Pair<T, R> pair : arr) {
            if (pair.getValue1().compareTo(val) <= 0)
                ret_val = pair.getValue0();
            else break;
        }

        return ret_val;
    }
}
