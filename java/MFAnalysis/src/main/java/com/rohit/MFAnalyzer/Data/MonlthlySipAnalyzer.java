package com.rohit.MFAnalyzer.Data;

import com.rohit.MFAnalyzer.MyProperties;
import com.rohit.MFAnalyzer.MyProperties.FileProperty;
import com.rohit.MFAnalyzer.Utils.Memoize;
import com.rohit.MFAnalyzer.Utils.Utils;
import com.rohit.MFAnalyzer.Data.CashFlow.InvestmentSummary;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.control.Try;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
public class MonlthlySipAnalyzer {

    private static final int NO_DAYS_IN_MONTH = 30;

    private List<Map<LocalDate, EodPrice>> eod_price_maps = new ArrayList<>();
    private MyProperties properties;
    private final BiFunction<Integer, Double, Tuple2<Double, Double>[]>
            irr_array_generator = Memoize.memoize(Utils::getIrrArray);

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
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public EodPrice parseEodPriceFromCsv(DateTimeFormatter fmtr, String csv_line, FileProperty properties) {
        String[] fields = csv_line.split(",");
        if (fields.length <= Math.max(properties.getDate_index(), properties.getEod_price_index())) {
            return null;
        }
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

    private InvestmentSummary cashFlowSummary(
            Map<LocalDate, EodPrice> eod_prices,
            Stream<LocalDate> flow_dates
    ) {
        return flow_dates
                .map(date -> eod_prices.getOrDefault(date, null))
                .filter(Objects::nonNull)
                .map(eod_price -> new CashFlow(eod_price, 1000.0))
                .collect(InvestmentSummary::new, InvestmentSummary::accumulator, InvestmentSummary::combiner);
    }

    private InvestmentSummary calculateInvestmentSummary(
            Map<LocalDate, EodPrice> eod_prices,
            Tuple3<LocalDate, LocalDate, Integer> investment
    ) {
        LocalDate start_date = investment._1;
        LocalDate valuation_date = investment._2;
        int no_of_flows = investment._3;
        double no_years_of_investment = Period.between(start_date, valuation_date).getDays() / 365.0;

        Supplier<LocalDate> sup = () -> start_date.plusMonths(1);
        Stream<LocalDate> dates =
                Stream
                        .generate(sup)
                        .limit(no_of_flows);

        InvestmentSummary investmentSummary = cashFlowSummary(eod_prices, dates);

        Tuple2<Double, Double>[] irr_array = irr_array_generator.apply(no_of_flows, no_years_of_investment);

        EodPrice valuation_price = eod_prices.getOrDefault(valuation_date, null);

        if (valuation_price == null) {
            investmentSummary.setValue(0.0);
            investmentSummary.setXirr(-100.0);
        } else {
            investmentSummary.setValue(Utils.round(investmentSummary.getUnits() * valuation_price.getPrice()));
            investmentSummary.setXirr(Utils.lowerBound(irr_array, investmentSummary.getValue()));
        }

        return investmentSummary;

    }

    public Stream<String> getRollingSummaries(
            Map<LocalDate, EodPrice> eod_prices,
            int no_of_contributions) {

        return eod_prices.keySet().stream()
                .sorted(LocalDate::compareTo)
                .map(start_date -> Tuple.of(start_date, start_date.plusMonths(no_of_contributions), no_of_contributions))// Stream of rolling investment
                .map(investment -> calculateInvestmentSummary(eod_prices, investment))
                .filter(investment -> investment.getValue() > 0.0001)
                .map(InvestmentSummary::toString);
    }

    public String forAllSecurities(int rolling_months) {

        StringBuilder builder = new StringBuilder();
        builder.append(InvestmentSummary.header());
        builder.append("\n");

        eod_price_maps.stream()
                .flatMap(eodPrices -> getRollingSummaries(eodPrices, rolling_months))
                .forEach(s -> builder.append(s));

        return builder.toString();
    }


}
