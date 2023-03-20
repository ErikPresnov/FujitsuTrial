package com.example.SpringBoot;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeeCalculatorTest {

    private final FeeCalculator feeCalculator = new FeeCalculator();

    @Test
    void checkParams() {
        Exception exception = assertThrows(RuntimeException.class,
                () -> feeCalculator.checkParams(null, "unicycle"));
        assertEquals("Unknown vehicle type", exception.getMessage());

        exception = assertThrows(RuntimeException.class,
                () -> feeCalculator.checkParams("taru", null));
        assertEquals("Unknown location", exception.getMessage());
    }

    @Test
    void calculateBaseFee() {
        assertEquals(3.5,feeCalculator.calculateBaseFee("tartu", "car"));
        assertEquals(3.0,feeCalculator.calculateBaseFee("tartu", "scooter"));
        assertEquals(2.5,feeCalculator.calculateBaseFee("tartu", "bike"));
        assertEquals(4.0,feeCalculator.calculateBaseFee("tallinn", "car"));
        assertEquals(3.5,feeCalculator.calculateBaseFee("tallinn", "scooter"));
        assertEquals(3.0,feeCalculator.calculateBaseFee("tallinn", "bike"));
        assertEquals(3.0,feeCalculator.calculateBaseFee("pärnu", "car"));
        assertEquals(2.5,feeCalculator.calculateBaseFee("pärnu", "scooter"));
        assertEquals(2.0,feeCalculator.calculateBaseFee("pärnu", "bike"));
    }

    @Test
    void airTempExtraFee() {
        assertEquals(1, feeCalculator.airTempExtraFee("bike", -11));
        assertEquals(0.5, feeCalculator.airTempExtraFee("bike", -9));
        assertEquals(0, feeCalculator.airTempExtraFee("bike", 1));
    }

    @Test
    void windSpeedExtraFee() {
        assertEquals(0, feeCalculator.windSpeedExtraFee("bike", 0));
        assertEquals(0.5, feeCalculator.windSpeedExtraFee("bike", 11));
        assertEquals(0, feeCalculator.windSpeedExtraFee("scooter", 21));
        assertEquals(0, feeCalculator.windSpeedExtraFee("car", 21));

        Exception exception = assertThrows(RuntimeException.class,
                () -> feeCalculator.windSpeedExtraFee("bike", 21));
        assertTrue(exception.getMessage().contains("Usage of selected vehicle type is forbidden"));
    }

    @Test
    void phenomenonExtraFee() {
        assertEquals(1, feeCalculator.phenomenonExtraFee("scooter", "snow"));
        assertEquals(1, feeCalculator.phenomenonExtraFee("bike", "snow"));

        assertEquals(1, feeCalculator.phenomenonExtraFee("scooter", "sleet"));
        assertEquals(1, feeCalculator.phenomenonExtraFee("bike", "sleet"));

        assertEquals(0.5, feeCalculator.phenomenonExtraFee("scooter", "rain"));
        assertEquals(0.5, feeCalculator.phenomenonExtraFee("bike", "rain"));

        Exception exception = assertThrows(RuntimeException.class,
                () -> feeCalculator.phenomenonExtraFee("scooter", "glaze"));
        assertEquals("Usage of selected vehicle type is forbidden", exception.getMessage());

        exception = assertThrows(RuntimeException.class,
                () -> feeCalculator.phenomenonExtraFee("scooter", "hail"));
        assertEquals("Usage of selected vehicle type is forbidden", exception.getMessage());

        exception = assertThrows(RuntimeException.class,
                () -> feeCalculator.phenomenonExtraFee("scooter", "thunder"));
        assertEquals("Usage of selected vehicle type is forbidden", exception.getMessage());

        exception = assertThrows(RuntimeException.class,
                () -> feeCalculator.phenomenonExtraFee("bike", "glaze"));
        assertEquals("Usage of selected vehicle type is forbidden", exception.getMessage());

        exception = assertThrows(RuntimeException.class,
                () -> feeCalculator.phenomenonExtraFee("bike", "hail"));
        assertEquals("Usage of selected vehicle type is forbidden", exception.getMessage());

        exception = assertThrows(RuntimeException.class,
                () -> feeCalculator.phenomenonExtraFee("bike", "thunder"));
        assertEquals("Usage of selected vehicle type is forbidden", exception.getMessage());
    }
}