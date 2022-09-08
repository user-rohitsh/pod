package com.rohit.MFAnalyzer.Data;

import com.rohit.MFAnalyzer.MyProperties;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonlthlySipAnalyzerTest {

    @Test
    void addMissingEodPrices() {
        MonlthlySipAnalyzer analyzer = new MonlthlySipAnalyzer(new MyProperties());

        DateTimeFormatter fmtr = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US);
        LocalDate date1 = LocalDate.parse("20-04-2022", fmtr);
        LocalDate date2 = LocalDate.parse("21-04-2022", fmtr);

        List<EodPrice> prices = new ArrayList<>();

        EodPrice price1 = new EodPrice(
                "AAA",
                LocalDate.parse("20-04-2022", fmtr),
                123.5
        );
        prices.add(price1);

        EodPrice price2 = new EodPrice(
                "AAA",
                LocalDate.parse("22-04-2022", fmtr),
                12.5
        );
        prices.add(price2);

        EodPrice price3 = new EodPrice(
                "AAA",
                LocalDate.parse("25-04-2022", fmtr),
                123444.5
        );
        prices.add(price3);

        analyzer.addMissingEodPrices(prices);
        assertEquals(prices.size(), 6);

        prices.clear();

        analyzer.addMissingEodPrices(prices);
        assertEquals(prices.size(), 0);

        price1 = new EodPrice(
                "AAA",
                LocalDate.parse("25-04-2022", fmtr),
                123444.5
        );
        prices.add(price1);

        analyzer.addMissingEodPrices(prices);
        assertEquals(prices.size(), 1);

    }
}