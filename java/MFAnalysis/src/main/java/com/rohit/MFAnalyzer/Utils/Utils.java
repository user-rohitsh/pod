package com.rohit.MFAnalyzer.Utils;

import io.vavr.CheckedFunction1;
import io.vavr.control.Try;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

    public static double annuityDueFV(double rate_percent, double annual_in_flows, int years) {
        if (rate_percent == 0) return annual_in_flows * years;
        double rate = rate_percent / 100.0;
        double fv = Math.pow(1 + rate, years) - 1;
        fv = fv / rate;
        fv = fv * annual_in_flows;
        fv = fv * (1 + rate);
        return Math.round(fv * 100.0) / 100.0;
    }

    //generate an array based on function f in domain [start, end); increment values by incrementer
    public static <T extends Comparable<T>, R> R[] ArrayFromFunction(Function<T, R> f, T start, T end, UnaryOperator<T> incrementer) {
        ArrayList<R> ret = new ArrayList<>();

        T temp = start;
        while (temp.compareTo(end) < 0) {
            ret.add(f.apply(temp));
            incrementer.apply(temp);
        }

        return (R[]) ret.toArray();
    }

    //first index with value greater than or equal to val
    public static <T extends Comparable<T>> int lowerBound(T[] arr, T val) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].compareTo(val) < 0) continue;
            return i;
        }
        return -1;
    }

    //first index with value greater than val
    public static <T extends Comparable<T>> int upperBound(T[] arr, T val) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].compareTo(val) > 0) continue;
            return i;
        }
        return -1;
    }
}
