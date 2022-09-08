package com.rohit.MFAnalyzer.Data;

import com.rohit.MFAnalyzer.MyProperties;
import com.rohit.MFAnalyzer.Utils.Utils;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
public class MonlthlySipAnalyzer {

    private static final int NO_DAYS_IN_MONTH = 30;

    private Map<LocalDate, EodPrice> eod_prices;
    private MyProperties properties;

    @Autowired
    public MonlthlySipAnalyzer(MyProperties properties) {
        this.properties = properties;
        Try<Stream<String>> checked_csv_lines = Utils.getLines(properties.getData_dir());

        if (checked_csv_lines.isFailure()) return;

        List<EodPrice> eod_prices_temp = parseEodPrices(checked_csv_lines.get());
        addMissingEodPrices(eod_prices_temp);

        eod_prices = eod_prices_temp
                .stream()
                .collect(Collectors.toMap(EodPrice::getDate, Function.identity()));
    }

    public List<EodPrice> parseEodPrices(Stream<String> lines) {
        String format = properties.getDate_format();
        DateTimeFormatter fmtr = DateTimeFormatter.ofPattern(format, Locale.US);

        return lines
                .map(line -> parseEodPriceFromCsv(fmtr, line))
                .collect(Collectors.toList());
    }

    public EodPrice parseEodPriceFromCsv(DateTimeFormatter fmtr, String csv_line) {
        String[] fields = csv_line.split(",");
        LocalDate date = LocalDate.parse(fields[properties.getDate_index()], fmtr);
        Double price = Double.parseDouble(fields[properties.getEod_price_index()]);
        return new EodPrice(properties.getSecurity_name(), date, price);
    }

    public void addMissingEodPrices(List<EodPrice> eod_prices) {

        if (eod_prices == null || eod_prices.size() <= 0) return;

        eod_prices.sort(EodPrice::compareTo);

        List<EodPrice> added = new ArrayList<>();

        EodPrice from = eod_prices.get(0);
        for (EodPrice eod_price : eod_prices) {
            LocalDate to = eod_price.getDate();
            long days = ChronoUnit.DAYS.between(from.getDate(), to);
            for (long day = 1; day < days; day++) {
                added.add(
                        new EodPrice(
                                from.getSecurity_name(),
                                from.getDate().plusDays(day),
                                from.getPrice()));
            }
            from = eod_price;
        }

        eod_prices.addAll(added);
    }


    public CashFlow sip_returns(LocalDate sip_start_date, int years) {
        int no_of_sips = years * 12;
        LocalDate sip_end_date = sip_start_date.plusDays(no_of_sips * NO_DAYS_IN_MONTH - 1);
        LocalDate sip_valuation_date = sip_start_date.plusDays(no_of_sips * NO_DAYS_IN_MONTH);
        double valuation_price = eod_prices.get(sip_valuation_date).getPrice();

        return Stream
                .iterate(sip_end_date, date -> date.plusDays(NO_DAYS_IN_MONTH))
                .limit(no_of_sips)
                .map(eod_prices::get)
                .map(eod_price -> new CashFlow(eod_price, 1000.0))
                .collect(CashFlow::new, CashFlow::accumulator, CashFlow::accumulator)
                .value(sip_start_date,sip_end_date,sip_valuation_date,valuation_price);
    }


    public String getSipArray( int years) {

        StringBuilder builder = new StringBuilder();

        eod_prices.keySet()
                .stream()
                .sorted(LocalDate::compareTo)
                .map( sip_start_date -> sip_returns(sip_start_date,years))
                .map(CashFlow::toString)
                .forEach(
                        flow-> builder.append(flow)
                );

        return builder.toString();
    }

}
