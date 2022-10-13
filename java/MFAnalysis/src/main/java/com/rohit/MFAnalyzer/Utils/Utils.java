package com.rohit.MFAnalyzer.Utils;

import io.vavr.CheckedFunction1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

    //FV of an annuity with <no_of_cash_flow_months> monthly flows kept till <no_years_of_investment>
    public static double futureValue(double rate_percent, Integer no_of_cash_flow_months, Double no_years_of_investment) {
        double rate = rate_percent / 100.0;
        return Utils.round(IntStream.range(0, no_of_cash_flow_months)
                .filter(month -> no_years_of_investment - month / 12.0 > 0.00001)
                .mapToDouble(month -> 1000.0 * Math.pow(1 + rate, no_years_of_investment - month / 12.0))
                .sum());
    }

    //generate an array of futureValues in domain [start, end);
    public static List<Tuple2<Double, Double>>
    getIrrArray(Integer no_of_cash_flow_months, Double no_years_of_investment) {

        return
                IntStream.range(-50, 50)
                        .mapToDouble(i -> i / 1.0)
                        .mapToObj(d -> Tuple.of(d, futureValue(d, no_of_cash_flow_months, no_years_of_investment)))
                        .collect(Collectors.toList());
    }

    //first index with value greater than or equal to val
    public static Optional<Double> lowerBound(List<Tuple2<Double, Double>> arr, Double val) {

        Double ret_val = null;
        for (Tuple2<Double, Double> pair : arr) {
            if (pair._2.compareTo(val) <= 0)
                ret_val = pair._1();
            else break;
        }
        return Optional.ofNullable(ret_val);
    }

    public static double round(double val) {
        return Math.round(val * 1000.0) / 1000.0;
    }
}
