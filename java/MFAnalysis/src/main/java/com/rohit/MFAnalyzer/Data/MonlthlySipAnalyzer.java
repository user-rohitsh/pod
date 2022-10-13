package com.rohit.MFAnalyzer.Data;

import com.rohit.MFAnalyzer.Data.CashFlow.InvestmentSummary;
import com.rohit.MFAnalyzer.MyProperties;
import com.rohit.MFAnalyzer.MyProperties.FileProperty;
import com.rohit.MFAnalyzer.Utils.Memoize;
import com.rohit.MFAnalyzer.Utils.Utils;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


@Component
public class MonlthlySipAnalyzer {

    private static final int NO_DAYS_IN_MONTH = 30;
    private final Logger logger;

    private List<Map<LocalDate, EodPrice>> eod_price_maps = new ArrayList<>();
    private MyProperties properties;
    private final BiFunction<Integer, Double, List<Tuple2<Double, Double>>>
            irr_array_generator = Memoize.memoize(Utils::getIrrArray);

    @Autowired
    public MonlthlySipAnalyzer(MyProperties properties) {

        logger = LoggerFactory.getLogger(this.getClass());

        this.properties = properties;
        this.properties.getFile_properties().stream().forEach(
                file_property -> {

                    Try<Stream<String>> checked_csv_lines = Utils.getLines(file_property.getData_dir());

                    if (checked_csv_lines.isFailure()) return;

                    List<EodPrice> eod_prices_temp = parseEodPrices(checked_csv_lines.get(), file_property);
                    addMissingEodPrices(eod_prices_temp);

                    eod_prices_temp.sort(EodPrice::compareTo);

                    logger.info("Adding rows " + eod_prices_temp.size());

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
        try {
            LocalDate date = LocalDate.parse(fields[properties.getDate_index()], fmtr);
            Double price = Double.parseDouble(fields[properties.getEod_price_index()]);
            return new EodPrice(properties.getSecurity_name(), date, price);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        return null;

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

    public String calculateSipXirr(
            LocalDate start_date,
            int no_of_flows,
            LocalDate valuation_date,
            Map<LocalDate, EodPrice> eod_prices
    ) {

        if (valuation_date == null)
            valuation_date = start_date.plusMonths(no_of_flows);

        EodPrice valuation_price = eod_prices.getOrDefault(valuation_date, null);
        if (valuation_price == null) return "";

        if ( eod_prices.getOrDefault(start_date, null) == null) return "";

        double no_years_of_investment = ChronoUnit.DAYS.between(start_date, valuation_date) / 365.0;

        Stream<LocalDate> dates = IntStream.range(0, no_of_flows)
                .mapToObj(i -> start_date.plusMonths(i));

        InvestmentSummary summ = cashFlowSummary(eod_prices, dates);
        summ.setValue(Utils.round(summ.getUnits() * valuation_price.getPrice()));

        List<Tuple2<Double, Double>> irr_array = irr_array_generator.apply(no_of_flows, no_years_of_investment);

        summ.setXirr(Utils.lowerBound(irr_array, summ.getValue()).orElseGet(() -> Double.MIN_VALUE));
        summ.setValuation_date(valuation_date);

        return summ.toString();

    }

    public String getRollingSipXirr(
            Map<LocalDate, EodPrice> eod_prices,
            int rolling_months) {

        Function<LocalDate, String> calculateSipIrr
                = (LocalDate start_date) -> calculateSipXirr(
                start_date, rolling_months, start_date.plusMonths(rolling_months), eod_prices);

        return eod_prices.keySet().stream()
                .sorted(LocalDate::compareTo)
                .map(start_date -> calculateSipIrr.apply(start_date))
                .filter(Objects::nonNull)
                .filter(s -> s.equals("") == false)
                .collect(Collectors.joining("\n"));
    }

    public String forAllSecurities(Function<Map<LocalDate, EodPrice>, String> lambda) {

        StringBuilder builder = new StringBuilder();
        builder.append(InvestmentSummary.header());
        builder.append("\n");

        builder.append(eod_price_maps.stream()
                .map(eodPrices -> lambda.apply(eodPrices))
                .filter(Objects::nonNull)
                .filter(s -> s.equals("") == false)
                .collect(Collectors.joining("\n")));

        builder.append('\n');
        return builder.toString();
    }


}
