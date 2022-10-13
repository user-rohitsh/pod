package com.rohit.TestService.RestClients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "TestService1Client", url = "${service1.url}")
public interface TestService1 {

    @RequestMapping(method = RequestMethod.GET, value = "/get")
    String get();

}

