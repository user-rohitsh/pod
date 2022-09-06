package com.rohit.MFAnalyzer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @GetMapping("/sip_returns")
    String siptReturns(@RequestParam("years") int number_of_years)
    {
        //return                timeSeries.getSipArray(0,number_of_years);
        return null;
    }
}
