package com.rohit.MFAnalyzer.Data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class EodPriceData {
    private LocalDate date;
    private Double eod_price;
}
