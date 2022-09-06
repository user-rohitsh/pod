package com.rohit.MFAnalyzer.Data;

import com.rohit.MFAnalyzer.Utils.Utils;
import io.vavr.Tuple;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


@Component("GoldData")
@PropertySource("application.properties")
public class MonlthlySipAnalyzer {

    private static final int NO_OF_MONTH_IN_YEAR = 12;
    private static final int NO_DAYS_IN_MONTH = 30;

    //private final EodPriceData[] eod_values;
    ArrayList<EodPriceDataFactory.EodPriceData> eod_data = new ArrayList<>();

    @Autowired
    public MonlthlySipAnalyzer(EodPriceDataFactory priceDataTemplate)
            throws IOException {

        try (Stream<Path> paths = Files.walk(FileSystems.getDefault().getPath("/tmp/gold.csv").toAbsolutePath())) {
            eod_data.addAll(paths
                    .map(Utils::getLines)
                    .filter(Try::isSuccess)
                    .flatMap(Try::get)
                    .map(line -> priceDataTemplate.getEodPriceDataFromCsvLine(line))
                    .sorted(EodPriceDataFactory.EodPriceData::compareTo)
                    .collect(Collectors.toList()));

        }

    }

    public double monthly_rate_of_return_of_sip(int startDateIndex, int durationInYears) {
        int no_of_months = durationInYears * NO_OF_MONTH_IN_YEAR;
        int endIndex = startDateIndex + no_of_months * NO_DAYS_IN_MONTH;

        double accumulated_units = IntStream.range(startDateIndex, endIndex)
                .filter(i -> (i - startDateIndex) % NO_DAYS_IN_MONTH == 0)
                .filter(i -> i < eod_data.size())
                .mapToObj(i -> Tuple.of(i,100.0 / eod_data.get(i).getEod_price()));

        double fv_et_end = accumulated_units * eod_data.get(endIndex).getEod_price();

        return Utils.rateFromAnnuity(fv_et_end, no_of_months, 100).doubleValue();
    }

    public String getSipArray(int startIndex, int durationInYears) {
        int lastIndex = eod_data.size() - durationInYears * NO_OF_MONTH_IN_YEAR;

        StringBuilder builder = new StringBuilder();
        IntStream.range(startIndex, lastIndex)
                .mapToDouble(i -> monthly_rate_of_return_of_sip(i, durationInYears))
                .map(d -> Math.round(10000 * d) / 100.0)
                .forEach(
                        v -> builder.append(v).append("\n")
                );

        return builder.toString();
    }

}
