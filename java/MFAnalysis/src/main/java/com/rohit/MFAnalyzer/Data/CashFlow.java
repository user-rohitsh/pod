package com.rohit.MFAnalyzer.Data;

import com.rohit.MFAnalyzer.Utils.Utils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.stream.IntStream;

@Data
@NoArgsConstructor
public class CashFlow {

    private String security_name;
    private double flow;
    private LocalDate date;
    private double price;
    private double units;

    @Data
    public static class Valuation {
        private double price;
        private LocalDate cash_flow_start_date;
        private LocalDate cash_flow_end_date;
        private LocalDate cash_flow_value_date;
        private double value;
        private int indicative_xirr;
    }

    private Valuation valuation = new Valuation();


    public CashFlow(EodPrice eod_price, double flow) {
        this.security_name = eod_price.getSecurity_name();
        this.date = eod_price.getDate();
        this.flow = flow;
        this.price = eod_price.getPrice();
        this.units = this.flow / this.price;
    }

    public CashFlow value(
            LocalDate cash_flow_start_date,
            LocalDate cash_flow_end_date,
            LocalDate cash_flow_value_date,
            double price) {
        this.valuation.price = price;
        this.valuation.value = units * price;
        this.valuation.cash_flow_end_date = cash_flow_end_date;
        this.valuation.cash_flow_start_date = cash_flow_start_date;
        this.valuation.cash_flow_value_date = cash_flow_value_date;

        return this;
    }

    public void accumulator(CashFlow second) {
        security_name = security_name == null ? second.security_name : security_name;
        flow = flow + second.flow;
        units = units + second.units;
    }

    @Override
    public String toString() {
        return ""
                + security_name + ","
                + flow + ","
                + this.valuation.cash_flow_start_date + ","
                + this.valuation.cash_flow_end_date + ","
                + this.valuation.cash_flow_value_date + ","
                + units + ","
                + this.valuation.value + ","
                + this.valuation.getPrice() + ","
                + this.valuation.indicative_xirr;
    }

    public static String header() {
        return "Security_name,"
                + "Flow,"
                + "Cash_flow_start_date,"
                + "Cash_flow_end_date,"
                + "Cash_flow_value_date,"
                + "Unit,"
                + "Value,"
                + "Valuation_price,"
                +"Indicative_xirr";
    }
}

