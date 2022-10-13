package com.rohit.MFAnalyzer;

import com.rohit.MFAnalyzer.Data.EodPrice;
import com.rohit.MFAnalyzer.Data.MonlthlySipAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

@RestController
public class Controller {

    MonlthlySipAnalyzer analyzer;
    private MyProperties properties;

    Logger logger ;

    @Autowired
    public Controller(MonlthlySipAnalyzer analyzer,
                      MyProperties properties) throws ExecutionException, InterruptedException {
        this.properties = properties;
        this.analyzer = analyzer;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @GetMapping("/rolling_returns")
    String rolling_returns(
            @RequestParam(name = "months", required = true) int months) {
        Function<Map<LocalDate, EodPrice>, String> lambda = prices -> analyzer.getRollingSipXirr(prices, months);

        return analyzer.forAllSecurities(lambda);
    }

    @GetMapping("/returns")
    String returns(
            @RequestParam(name = "start", required = true) String start,
            @RequestParam(name = "end", required = true) String end
    ) {
        DateTimeFormatter fmtr = DateTimeFormatter.ofPattern("dMMyy", Locale.US);
        LocalDate start_date = LocalDate.parse(start, fmtr);
        LocalDate end_date = LocalDate.parse(end, fmtr);

        Function<Map<LocalDate, EodPrice>, String> lambda = prices -> analyzer.calculateSipXirr(
                start_date,1,end_date,prices
        );

        return analyzer.forAllSecurities(lambda);
    }

    @GetMapping("/sip_returns")
    String sip_returns(
            @RequestParam(name = "start", required = true) String start,
            @RequestParam(name = "no_of_flows", required = true) Integer no_of_months,
            @RequestParam(name = "value_date", required = false) String value_dt
    ) {
        DateTimeFormatter fmtr = DateTimeFormatter.ofPattern("dMMyy", Locale.US);
        LocalDate start_date = LocalDate.parse(start, fmtr);
        LocalDate value_date =
                value_dt == null ? start_date.plusMonths(no_of_months) : LocalDate.parse(value_dt, fmtr);

        Function<Map<LocalDate, EodPrice>, String> lambda = prices -> analyzer.calculateSipXirr(
                start_date,no_of_months,value_date,prices
        );

        return analyzer.forAllSecurities(lambda);
    }
}
