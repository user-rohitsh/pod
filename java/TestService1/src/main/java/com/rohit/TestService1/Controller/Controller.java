package com.rohit.TestService1.Controller;

import com.rohit.TestService1.RestClients.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @Autowired
    TestService service;

    @GetMapping("/get")
    String get() {
        return " Service 1";
    }
}
