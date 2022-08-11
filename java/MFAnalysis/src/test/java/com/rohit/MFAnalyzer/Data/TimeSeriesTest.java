package com.rohit.MFAnalyzer.Data;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

class TimeSeriesTest {

    @Test
    void monthly_rate_of_return_of_sip() {
        double[] data = {
                100, 120, 105, 125, 127, 96, 110, 120, 130, 140, 120, 155, 160
        };

        TimeSeries ts = new TimeSeries(data);

        double rate = ts.monthly_rate_of_return_of_sip(0,1);

        assertEquals(.0451,rate,0.01);
    }

    @Test
    void getSipArray() {
        double[] data = {
                100, 120, 105, 125, 127, 96, 110, 120, 130, 140, 120, 155, 160, 170, 180, 190, 200, 210
        };

        TimeSeries ts = new TimeSeries(data);

        double[] rates = ts.getSipArray(0,1);

        assertNotNull(rates);
    }
}