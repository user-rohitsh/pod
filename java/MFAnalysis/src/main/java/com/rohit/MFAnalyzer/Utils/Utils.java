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
        return paths.filter(Files::isRegularFile).flatMap(path -> {
            try {
                return Files.lines(path).skip(1);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        });
    }


}
