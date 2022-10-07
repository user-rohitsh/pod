package com.rohit.TestService.Controller;

import com.rohit.TestService.RestClients.TestService1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @Autowired
    TestService1 service1;

    @Value("${service1.url}")
    String url;

    @GetMapping("/get")
    String get() {
        System.out.println("url =" + url);
        String reply = service1.get();
        return "From Service - Reply received from " + reply;
    }
}
