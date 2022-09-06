package com.rohit.MFAnalyzer.Data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class EodPriceData {
    private LocalDate date;
    private Double eod_price;

    public int compareTo(EodPriceData second)
    {
        return this.date.compareTo(second.date);
    }
}
