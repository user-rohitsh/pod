package com.rohit.MFAnalyzer.Utils;

import io.vavr.CheckedFunction1;
import io.vavr.control.Try;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

public class Utils {

    static Pair<BigDecimal, BigDecimal> quadraticSolver(double a, double b, double c) {

        double D = b * b - 4 * a * c;

        if (D < 0.0) return null;

        BigDecimal root1 = new BigDecimal((-b + Math.sqrt(D)) / (2 * a));
        BigDecimal root2 = new BigDecimal((-b - Math.sqrt(D)) / (2 * a));

        return Pair.of(root1.max(root2), root1.min(root2));
    }

    public static BigDecimal rateFromAnnuity(double fv_at_end_periods, int no_of_periods, double pmt_start_of_period) {
        double a = pmt_start_of_period * (no_of_periods - 2) * (no_of_periods - 1) * no_of_periods / 6.0;
        double b = pmt_start_of_period * (no_of_periods - 1) * no_of_periods / 2.0;
        double c = pmt_start_of_period * no_of_periods - fv_at_end_periods;

        Pair<BigDecimal, BigDecimal> solution = quadraticSolver(a, b, c);

        if (solution == null) return new BigDecimal(10000.0);

        return solution.getLeft();
    }

    public static Try<Stream<String>> getLines(String dir) {
        Path p = FileSystems.getDefault().getPath(dir);
        Try<Stream<Path>> checked_paths = CheckedFunction1.liftTry((Path fp) -> Files.walk(fp)).apply(p);

        Function<Stream<Path>, Try<Stream<String>>> checkedConvertPathsToStreams = CheckedFunction1.liftTry((Stream<Path> paths) -> Utils.convertPathsToLines(paths));
        return checked_paths.flatMap( paths -> checkedConvertPathsToStreams.apply(paths));
    }

    public static Stream<String> convertPathsToLines(Stream<Path> paths)
    {
        return paths.filter(Files::isRegularFile).flatMap(path -> {
            try {
                return Files.lines(path).skip(1);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        });
    }


}
