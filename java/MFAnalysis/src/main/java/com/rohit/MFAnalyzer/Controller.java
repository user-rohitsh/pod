package com.rohit.MFAnalyzer;

import com.rohit.MFAnalyzer.Data.MonlthlySipAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class Controller {

    MonlthlySipAnalyzer analyzer;
    KafkaTemplate<String, String> kafka_template;
    private MyProperties properties;

    @Autowired
    public Controller(MonlthlySipAnalyzer analyzer,
                      KafkaTemplate<String, String> kafka_template,
                      MyProperties properties) {
        this.properties = properties;
        this.analyzer = analyzer;
        this.kafka_template = kafka_template;
    }

    @GetMapping("/sip_returns/{years}")
    String sipReturns(@PathVariable int years) {
        String results = analyzer.getSipArray(years);
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request =
                new HttpEntity<String>("{ " + results.length() + "}", headers);

        System.out.println("Sending data to file persitence" + results);
        restTemplate.postForEntity(properties.getFile_persistence_service(), request, String.class);
        kafka_template.send("results", results);
        return results;
    }
}
