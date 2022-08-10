package com.rohit.MFAnalyzer.Utils;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.Optional;

public class Utils {

    public static Pair<BigDecimal,BigDecimal> quadraticSolver(double a, double b, double c) {

        double D = b * b - 4 * a * c;

        if ( D < 0.0) return null;

        BigDecimal root1 = new BigDecimal((-b + Math.sqrt(D))/(2*a));
        BigDecimal root2 = new BigDecimal((-b - Math.sqrt(D))/(2*a));

        return Pair.of(root1.max(root2),root1.min(root2));
    }

    public static Optional<BigDecimal> annualizedRate(double fv, int t, double pmt)
    {
        double a = pmt * (t-2)*(t-1)*t/6.0;
        double b = pmt * (t-1)*t/2.0;
        double c = pmt * t - fv;

        Pair<BigDecimal,BigDecimal> solution = quadraticSolver(a,b,c);

        if ( solution == null) return Optional.empty();

        return Optional.of(solution.getLeft());
    }
}
