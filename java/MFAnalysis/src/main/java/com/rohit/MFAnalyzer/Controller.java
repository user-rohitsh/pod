package com.rohit.MFAnalyzer;

import com.rohit.MFAnalyzer.Data.MonlthlySipAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @Autowired
    MonlthlySipAnalyzer analyzer;

    @GetMapping("/sip_returns/{years}")
    String sipReturns(@PathVariable int years)
    {
        return analyzer.getSipArray(years);
    }
}
