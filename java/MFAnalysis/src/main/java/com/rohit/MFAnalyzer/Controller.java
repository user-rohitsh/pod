package com.rohit.MFAnalyzer;

import com.rohit.MFAnalyzer.Data.MonlthlySipAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

@RestController
public class Controller {

    MonlthlySipAnalyzer analyzer;
    private MyProperties properties;

    @Autowired
    public Controller(MonlthlySipAnalyzer analyzer,
                      MyProperties properties) throws ExecutionException, InterruptedException {
        this.properties = properties;
        this.analyzer = analyzer;
    }

    @GetMapping("/rolling_returns")
    String rolling_returns(
            @RequestParam(name = "months", required = true) int months) {
        return analyzer.getRollingSummariesForAllSecurities(months);
    }

    @GetMapping("/returns")
    String returns(
            @RequestParam(name = "start", required = true) String start,
            @RequestParam(name = "end", required = true) String end
    ) {
        DateTimeFormatter fmtr = DateTimeFormatter.ofPattern("dMMyy", Locale.US);
        LocalDate start_date = LocalDate.parse(start, fmtr);
        LocalDate end_date = LocalDate.parse(end, fmtr);
        return analyzer.getAbsoluteReturn(start_date, end_date);
    }

    @GetMapping("/returns")
    String sip_returns(
            @RequestParam(name = "start", required = true) String start,
            @RequestParam(name = "end_date", required = true) String end,
            @RequestParam(name = "value_date", required = false) String value_dt
    ) {
        DateTimeFormatter fmtr = DateTimeFormatter.ofPattern("dMMyy", Locale.US);
        LocalDate start_date = LocalDate.parse(start, fmtr);
        LocalDate end_date = LocalDate.parse(end, fmtr);
        LocalDate value_date = value_dt == null ? end_date.plusMonths(1): LocalDate.parse(value_dt, fmtr);

        return analyzer.
    }
}
