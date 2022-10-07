package com.rohit.MFAnalyzer.Data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class CashFlow {

    private String security_name;
    private double flow;
    private LocalDate date;
    private double price;

    @Data
    public static class InvestmentSummary {
        private String security_name;
        private LocalDate start_date;
        private LocalDate valuation_date;
        private int no_of_flows = 0;
        private double units;
        private double value;
        private double xirr;

        public void accumulator(CashFlow flow) {
            security_name = security_name == null ? flow.security_name : security_name;
            start_date = start_date == null ? flow.date : start_date;
            no_of_flows++;
            units = units + flow.flow / flow.price;
        }

        public InvestmentSummary combiner(InvestmentSummary investmentSummary) {
            return this;
        }

        @Override
        public String toString() {
            return ""
                    + security_name + ","
                    + this.start_date + ","
                    + this.valuation_date + ","
                    + this.no_of_flows + ","
                    + this.units + ","
                    + this.value + ","
                    + this.xirr;
        }

        public static String header() {
            return "Security,"
                    + "Start Date,"
                    + "Value Date,"
                    + "No Of Flows,"
                    + "Units,"
                    + "Value,"
                    + "XIRR";
        }

    }


    public CashFlow(EodPrice eod_price, double flow) {
        this.security_name = eod_price.getSecurity_name();
        this.date = eod_price.getDate();
        this.flow = flow;
        this.price = eod_price.getPrice();
    }

}

