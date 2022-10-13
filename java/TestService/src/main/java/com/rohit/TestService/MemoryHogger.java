package com.rohit.TestService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;


@Component
public class MemoryHogger {

    List<List<Integer>> strs = new ArrayList<>();
    Random random = new Random(100);
    int counter = 0;


    Logger logger = LoggerFactory.getLogger(this.getClass());


    @Scheduled(fixedRate = 5000)
    public void task() {

        if (counter < 10) {
            strs.add(Arrays.asList(new Integer[1024 * 1024 * 10]));
            logger.info("Allocated 1");
        }
        else if (strs.size() >= 1) {
            strs.remove(1);
            logger.info("De Allocated 1");
        }

        counter++;
    }
}
