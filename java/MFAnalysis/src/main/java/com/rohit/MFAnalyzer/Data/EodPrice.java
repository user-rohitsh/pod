package com.rohit.MFAnalyzer.Data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class EodPrice {
    private String security_name;
    private LocalDate date;
    private double price;

    public int compareTo(EodPrice second) {
        return this.date.compareTo(second.date);
    }


}
