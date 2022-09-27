package com.rohit.MFAnalyzer;

import com.rohit.MFAnalyzer.Data.MonlthlySipAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/sip_returns/{years}")
    String sipReturns(@PathVariable int years) throws Exception {
        String results = analyzer.getSipDataFromPriceMaps(years);
        return results;
    }
}
