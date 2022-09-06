package com.rohit.MFAnalyzer.Data;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvToBeanBuilder;
import com.rohit.MFAnalyzer.Utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.stream.IntStream;


@Component("GoldData")
public class EodAnalysis {

    private static final int NO_OF_MONTH_IN_YEAR = 12;
    private static final int NO_DAYS_IN_MONTH = 30;

    @Autowired
    public EodAnalysis(){

    }

    public EodAnalysis(double[] data) {

    }


//    public double monthly_rate_of_return_of_sip(int startDateIndex, int durationInYears) {
//        int no_of_months = durationInYears * NO_OF_MONTH_IN_YEAR;
//        int endIndex = startDateIndex + no_of_months;
//
//        double accumulated_units = IntStream.range(startDateIndex, endIndex)
//                .mapToDouble(i -> 100.0 / monthly_navs[i])
//                .sum();
//
//        double fv_et_end = accumulated_units * monthly_navs[endIndex];
//
//        return Utils.rateFromAnnuity(fv_et_end, no_of_months, 100).doubleValue();
//    }
//
//    public double[] getSipArray(int startIndex, int durationInYears) {
//        int lastIndex = monthly_navs.length - durationInYears * NO_OF_MONTH_IN_YEAR;
//
//        return IntStream.range(startIndex, lastIndex)
//                .mapToDouble(i -> monthly_rate_of_return_of_sip(i, durationInYears))
//                .map(monthly_rate -> Math.round(10000 * monthly_rate) / 100.0)
//                .toArray();
//    }

}
