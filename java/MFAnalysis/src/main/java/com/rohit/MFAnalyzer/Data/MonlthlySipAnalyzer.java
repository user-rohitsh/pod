package com.rohit.MFAnalyzer.Data;

import com.rohit.MFAnalyzer.MyProperties;
import com.rohit.MFAnalyzer.MyProperties.FileProperty;
import com.rohit.MFAnalyzer.Utils.Utils;
import io.vavr.control.Try;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
public class MonlthlySipAnalyzer {

    private static final int NO_DAYS_IN_MONTH = 30;

    private List<Map<LocalDate, EodPrice>> eod_price_maps = new ArrayList<>();
    private MyProperties properties;

    @Autowired
    public MonlthlySipAnalyzer(MyProperties properties) {

        this.properties = properties;
        this.properties.getFile_properties().stream().forEach(
                file_property -> {

                    Try<Stream<String>> checked_csv_lines = Utils.getLines(file_property.getData_dir());

                    if (checked_csv_lines.isFailure()) return;

                    List<EodPrice> eod_prices_temp = parseEodPrices(checked_csv_lines.get(), file_property);
                    addMissingEodPrices(eod_prices_temp);

                    eod_prices_temp.sort(EodPrice::compareTo);

                    eod_price_maps.add(
                            eod_prices_temp.stream()
                                    .collect(Collectors.toMap(EodPrice::getDate, Function.identity(), (e1, e2) -> e1)));

                    checked_csv_lines.get().close();
                });
    }

    public List<EodPrice> parseEodPrices(Stream<String> lines, FileProperty properties) {
        String format = properties.getDate_format();
        DateTimeFormatter fmtr = DateTimeFormatter.ofPattern(format, Locale.US);

        return lines
                .map(line -> parseEodPriceFromCsv(fmtr, line, properties))
                .collect(Collectors.toList());
    }

    public EodPrice parseEodPriceFromCsv(DateTimeFormatter fmtr, String csv_line, FileProperty properties) {
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


    private CashFlow sip_returns(
            Map<LocalDate, EodPrice> eod_prices,
            LocalDate sip_start_date,
            int years) {
        int no_of_sips = years * 12;
        LocalDate sip_end_date = sip_start_date.plusMonths(no_of_sips - 1);
        LocalDate sip_valuation_date = sip_start_date.plusMonths(no_of_sips);

        if (!eod_prices.containsKey(sip_end_date) || !eod_prices.containsKey(sip_valuation_date))
            return null;

        double valuation_price = eod_prices.get(sip_valuation_date).getPrice();

        CashFlow final_aggregated_cashflow = Stream
                .iterate(sip_start_date, date -> date.plusDays(NO_DAYS_IN_MONTH))
                .limit(no_of_sips)
                .map(eod_prices::get)
                .map(eod_price -> new CashFlow(eod_price, 1000.0))
                .collect(CashFlow::new, CashFlow::accumulator, CashFlow::accumulator)
                .value(sip_start_date, sip_end_date, sip_valuation_date, valuation_price);

        return final_aggregated_cashflow;
    }

    public Stream<String> getSipDataFromPriceMap(
            Map<LocalDate, EodPrice> eod_prices,
            int years,
            Pair<Double, Double>[] indicative_returns) {

        return eod_prices.keySet()
                .stream()
                .sorted(LocalDate::compareTo)
                .map(sip_start_date -> sip_returns(eod_prices, sip_start_date, years))
                .filter(Objects::nonNull)
                .map(cash_flow -> {
                    cash_flow.getValuation().setIndicative_xirr(
                            Utils.lowerBound(indicative_returns, cash_flow.getValuation().getValue())
                    );
                    return cash_flow;
                })
                .map(CashFlow::toString);
    }

    public String getSipDataFromPriceMaps(int years) {

        Pair<Double, Double>[] irr_array = Utils.ArrayFromFunction(
                r -> Utils.annuityDueFV(r, years),
                -20.0,
                20.0,
                r -> r + 0.25);

        StringBuilder builder = new StringBuilder()
                .append(CashFlow.header())
                .append('\n');

        eod_price_maps.stream()
                .flatMap(eodPrices -> getSipDataFromPriceMap(eodPrices, years, irr_array))
                .forEach(
                        flow -> {
                            builder.append(flow);
                            builder.append('\n');
                        });

        return builder.toString();
    }

}
