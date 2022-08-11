package com.rohit.MFAnalyzer.Data;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvToBeanBuilder;
import com.rohit.MFAnalyzer.Utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;


@Component("GoldData")
@PropertySource("application.properties")
public class TimeSeries {

    private static final int NO_OF_MONTH_IN_YEAR=12;
    private static final int NO_DAYS_IN_MONTH=30;

    public static class Ts_Data {
        @CsvBindByName(column = "Date", locale = "en-US")
        @CsvDate(value="dd-MMM-yy")
        private Date date;

        @CsvBindByName(column = "Spot Price(Rs.)")
        private double nav;
        public double getNav() {
            return nav;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Ts_Data ts_data = (Ts_Data) o;
            return date.equals(ts_data.date);
        }

        @Override
        public int hashCode() {
            return Objects.hash(date);
        }

        public static int compareTo(Ts_Data d1, Ts_Data d2) {
            return d1.date.compareTo(d2.date);
        }
    }

    private final double[] monthly_navs;

    @Autowired
    public TimeSeries(@Value("${ts_file_name}") String ts_file_name)
            throws FileNotFoundException {

        List<Ts_Data> temp = new CsvToBeanBuilder(new FileReader(ts_file_name))
                .withType(Ts_Data.class)
                .build()
                .parse();

        Set<Ts_Data> test = new HashSet<>();
        final double[] ts_data1 = temp
                .stream()
                .filter(test::add)
                .sorted(Ts_Data::compareTo)
                .mapToDouble(Ts_Data::getNav)
                .toArray();

        monthly_navs = IntStream.range(0,ts_data1.length)
                .filter(i -> i %NO_DAYS_IN_MONTH == 0)
                .mapToDouble(i -> ts_data1[i])
                .toArray();
    }

    public TimeSeries(double[] data)
    {
        monthly_navs = data;
    }


    public double monthly_rate_of_return_of_sip(int startDateIndex, int durationInYears) {
        int no_of_months = durationInYears * NO_OF_MONTH_IN_YEAR;
        int endIndex = startDateIndex + no_of_months;

        double accumulated_units = IntStream.range(startDateIndex, endIndex)
                .mapToDouble(i -> 100.0 / monthly_navs[i] )
                .sum();

        double fv_et_end = accumulated_units * monthly_navs[endIndex];

        return Utils.rateFromAnnuity(fv_et_end, no_of_months, 100).doubleValue();
    }

    public double[] getSipArray(int startIndex, int durationInYears) {
        int lastIndex = monthly_navs.length  - durationInYears * NO_OF_MONTH_IN_YEAR ;

        return IntStream.range(startIndex,lastIndex)
                .mapToDouble(i -> monthly_rate_of_return_of_sip(i,durationInYears))
                .map( monthly_rate -> Math.round(10000 * monthly_rate )/100.0)
                .toArray();
    }

}
