package com.rohit.MFAnalyzer.Utils;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void quadraticSolver() {
        Pair<BigDecimal,BigDecimal> sol = Utils.quadraticSolver(1,0,-1);
        assertEquals(Pair.of(new BigDecimal(1.0),new BigDecimal(-1.0)),sol);
    }

    @Test
    void annualizedRate() {
        BigDecimal sol = Utils.rateFromAnnuity(2666.73,24,100);

        assertEquals(0.00833,sol.doubleValue(),0.01);
    }
}