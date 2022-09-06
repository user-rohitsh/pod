package com.rohit.MFAnalyzer.Data;

import com.rohit.MFAnalyzer.MyProperties;
import com.rohit.MFAnalyzer.Utils.Utils;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;


@Component
public class MonlthlySipAnalyzer {

    private static final int NO_OF_MONTH_IN_YEAR = 12;
    private static final int NO_DAYS_IN_MONTH = 30;

    private final List<EodPriceData> eod_data = new ArrayList<>();
    private MyProperties  properties;

    @Autowired
    public MonlthlySipAnalyzer(MyProperties properties){
        this.properties=properties;
        Try<Stream<String>> checked_csv_lines = Utils.getLines(properties.getData_dir());
        checked_csv_lines.forEach(
                lines ->extractEodData(lines)
        );
        eod_data.sort(EodPriceData::compareTo);
        int a= 2;
    }

    public void extractEodData (Stream<String> lines)
    {
        String format = properties.getDate_format();
        DateTimeFormatter fmtr = DateTimeFormatter.ofPattern(format, Locale.US);

        lines.forEach(
                line -> eod_data.add(extractEodData(fmtr, line))
        );
    }

    public EodPriceData extractEodData(DateTimeFormatter fmtr, String csv_line)
    {
        String[] fields = csv_line.split(",");
        LocalDate date = LocalDate.parse(fields[properties.getDate_index()],fmtr);
        Double price = Double.parseDouble(fields[properties.getEod_price_index()]);
        return new EodPriceData(date,price);
    }

    /*
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
*/
}
