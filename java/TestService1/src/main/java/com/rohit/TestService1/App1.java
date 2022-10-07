package com.rohit.TestService1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class App1 {

    public static void main(String[] args) {
        SpringApplication.run(App1.class, args);
    }
}
