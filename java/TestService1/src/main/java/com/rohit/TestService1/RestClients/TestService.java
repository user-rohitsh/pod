package com.rohit.TestService1.RestClients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "TestServiceClient", url = "http://localhost:8080")
public interface TestService {

    @RequestMapping(method = RequestMethod.GET, value = "/get")
    String get();

}

