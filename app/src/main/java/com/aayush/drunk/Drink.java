package com.aayush.drunk;

import android.util.Log;

import java.util.Date;

import static com.aayush.drunk.Body.*;
import static com.aayush.drunk.Utils.ozToG;

/**
 * Created by account on 7/5/17.
 */

public class Drink {
    final static String BEER = "beer";
    final static String MALT = "malt liquor";
    final static String WINE = "wine";
    final static String FORTIFIED_WINE = "fortified wine";
    final static String LIQUEUR = "liqueur";
    final static String SHOT = "shot";
    String type;
    double fractionAlcohol;
    double sizeInG;
    Date timestamp;

    Drink(String type, double fractionAlcohol, double sizeInOz, Date timestamp) {
        this.type = type;
        this.timestamp = ((Date) timestamp.clone());
        init(fractionAlcohol, sizeInOz);
    }

    Drink(String type, Date timestamp) {
        this.type = type;
        this.timestamp = ((Date) timestamp.clone());
        switch (type) {
            case BEER:
                init(.05, 12);
                break;
            case MALT:
                init(.07, 8.5);
                break;
            case WINE:
                init(.12, 5);
                break;
            case FORTIFIED_WINE:
                init(.17, 3.5);
                break;
            case LIQUEUR:
                init(.24, 2.5);
                break;
            case SHOT:
                init(.4, 1.5);
                break;
            default:
                //TODO handle this?
        }
    }

    double currentBAC() {
        double initialGramsAlc = sizeInG * fractionAlcohol;
        double eliminationRate = Body.isAlcoholic ? .025 : .015; //TODO changes if you're hungry tho
        double hoursElapsed = (System.currentTimeMillis() - timestamp.getTime()) / 3600000.;
        double alc = initialGramsAlc / (r * weight)  - (bloodVolume / 100) * eliminationRate * hoursElapsed;
        alc /= 10;
        Log.d("uh oh", "" + alc + " " + initialGramsAlc);
        return alc > .0001 ? alc : 0;
    }

    private void init(double fraction_alcohol, double sizeInOz) {
        this.fractionAlcohol = fraction_alcohol;
        this.sizeInG = ozToG(sizeInOz);
    }
}