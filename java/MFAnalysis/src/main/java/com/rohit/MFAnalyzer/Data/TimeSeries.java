package com.rohit.MFAnalyzer.Data;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Component
@PropertySource("applications.properties")
public class TimeSeries {

    private static class Ts_Data {
        @CsvBindByName(column = "")
        @CsvDate("d-MMM-yy")
        private LocalDate date;

        @CsvBindByName(column = "")
        private double nav;

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

        public static int compareTo(Ts_Data d1, Ts_Data d2)
        {
            return d1.date.compareTo(d2.date);
        }
    }

    private final List<Ts_Data> ts_data;

    public TimeSeries(@Value("ts_file_name") String ts_file_name)
            throws FileNotFoundException {

        List<Ts_Data> temp = new CsvToBeanBuilder(new FileReader(ts_file_name))
                .withType(Ts_Data.class)
                .build()
                .parse();

        Set<Ts_Data> test = new HashSet<>();
        ts_data = temp
                .stream()
                .filter(test::add)
                .sorted(Ts_Data::compareTo)
                .collect(Collectors.toList());
    }


    public void sipReturns(int months)
    {

    }


}
