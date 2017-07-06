package com.aayush.drunk;

/**
 * Created by account on 7/4/17.
 */

public class Utils {
    final static double G_PER_OUNCE = 28.3495;
    final static double POUNDS_PER_KILO = 2.20462;
    final static double INCHES_PER_METER = 39.3701;


    static double poundsToKilos(double pounds) {
        return pounds / POUNDS_PER_KILO;
    }

    static double kilosToPounds(double kilos) {
        return kilos * POUNDS_PER_KILO;
    }

    static double inchesToMeters(double inches) {
        return inches / INCHES_PER_METER;
    }

    static double metersToInches(double meters) {
        return meters * INCHES_PER_METER;
    }

    static double ozToG(double oz) {
        return oz * G_PER_OUNCE;
    }

    static double gToOz(double g) {
        return g / G_PER_OUNCE;
    }
}
