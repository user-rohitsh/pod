package com.rohit.MFAnalyzer;

import com.rohit.MFAnalyzer.Data.MonlthlySipAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    MonlthlySipAnalyzer analyzer;
    KafkaTemplate<String,String> kafka_template;

    @Autowired
    public Controller(MonlthlySipAnalyzer analyzer, KafkaTemplate<String, String> kafka_template) {
        this.analyzer = analyzer;
        this.kafka_template = kafka_template;
    }

    @GetMapping("/sip_returns/{years}")
    String sipReturns(@PathVariable int years)
    {
        String results = analyzer.getSipArray(years);
        kafka_template.send("results",results);
        return results;
    }
}
