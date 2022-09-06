package com.rohit.MFAnalyzer;

import com.rohit.MFAnalyzer.Data.MonlthlySipAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class Controller {

    @Autowired
    @Qualifier("GoldData")
    private MonlthlySipAnalyzer timeSeries;

    @GetMapping("/sip_returns")
    String siptReturns(@RequestParam("years") int number_of_years)
    {
        return timeSeries.getSipArray(0,number_of_years);
    }
}
