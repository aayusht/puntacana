package com.aayush.drunk;

/**
 * Created by account on 7/4/17.
 */

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

/** The user's body
 *
 * Sources: <a href="https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4361698/#sec4-0025802414524385title">Alcohol calculations and their uncertainty</a>
 * <a href="http://www.nutrientsreview.com/alcohol/absorption-metabolism-elimination-factors.html">Alcohol elimination rate</a>
 * BAC = mass of alc in g / (r * mass of person in kilos) - elimination rate * hrs passed
 * r for men = 1.0181 - 0.01213 * (mass in kilos) / (height in meters squared)
 * r for women = 0.9367 - 0.01240 * (mass in kilos) / (height in meters squared)
 * elimination rate for empty stomach, .01 - .015 grams alcohol / 100ml blood / hr
 * elimination rate after meal, .015 - .020 grams alcohol / 100ml blood / hr
 * elimination after several weeks of alcoholism, .025 - .035 grams alcohol / 100ml blood / hr
 * blood volume for male in ml using in and lbs = .006012 * h^3 + 14.6 * w + 604
 * blood volume for female in ml using in and lbs = .005835 * h^3 + 15 * w + 183
 */
public class Body {
    final static String FILENAME = "bodydata.json";

    static double r = 0;
    static double weight = 0; //in kilos
    static double bloodVolume = 0; // in mL
    static boolean isAlcoholic = false;

    static ArrayList<Drink> drinks = new ArrayList<>();


    static double get_BAC() {
        Calendar c = Calendar.getInstance();
        Date currTime = c.getTime();
        double totalAlcohol = 0;
        for (int i = 0; i < 69 && i < drinks.size(); i++) {
            totalAlcohol += drinks.get(i).currentAlcInBlood(currTime);
        }
        return totalAlcohol / bloodVolume;
    }

    /**
     * Initializes static variables
     * @param weight user's weight
     * @param isMale user's sex (true for male, false for female)
     * @param height user's height
     * @param isAlcoholic lol
     * @param isWeightMetric kgs if true, lbs if false
     * @param isHeightMetric meters if true, inches if false
     */
    static void initialize(double weight, boolean isMale, double height, boolean isAlcoholic,
                           boolean isWeightMetric, boolean isHeightMetric) {
        double heightInMeters, heightInInches, weightInLbs, weightInKgs;
        if (isWeightMetric) {
            weightInKgs = weight;
            weightInLbs = Utils.kilosToPounds(weight);
        } else {
            weightInLbs = weight;
            weightInKgs = Utils.poundsToKilos(weight);
        }

        if (isHeightMetric) {
            heightInMeters = height / 100;
            heightInInches = Utils.metersToInches(height / 100);
        } else {
            heightInInches = height;
            heightInMeters = Utils.inchesToMeters(height);
        }

        if (isMale) {
            Body.bloodVolume = .006012 * Math.pow(heightInInches, 3) + 14.6 * weightInLbs + 604;
            Body.r = 1.0181 - 0.01213 * weightInKgs / Math.pow(heightInMeters, 2);
        } else {
            Body.bloodVolume = .005835 * Math.pow(heightInInches, 3) + 15 * weightInLbs + 183;
            Body.r = 0.9367 - 0.01240 * weightInKgs / Math.pow(heightInMeters, 2);
        }

        Body.weight = weightInKgs;
        Body.isAlcoholic = isAlcoholic;
    }

    static void save(Context context) {
        try {
            JSONObject json = new JSONObject();
            json.put("r", Body.r);
            json.put("weight", Body.weight);
            json.put("bloodVolume", Body.bloodVolume);
            json.put("isAlcoholic", Body.isAlcoholic);

            JSONArray drinksJSON = new JSONArray();
            for (Drink drink : drinks) {
                JSONObject drinkJSON = new JSONObject();
                drinkJSON.put("fractionAlcohol", drink.fractionAlcohol);
                drinkJSON.put("sizeInG", drink.sizeInG);
                drinkJSON.put("timestamp", drink.timestamp.getTime());
                drinksJSON.put(drinkJSON);
            }
            json.put("drinks", drinksJSON);

            Log.d("json to be saved", json.toString());

            FileOutputStream outputStream = context.openFileOutput(FILENAME, MODE_PRIVATE);
            outputStream.write(json.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static void load(Context context) {
        try {
            FileInputStream inputStream = context.openFileInput(FILENAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            Log.d("json to be loaded", sb.toString());
            JSONObject json = new JSONObject(sb.toString());

            Body.r = json.getDouble("r");
            Body.weight = json.getDouble("weight");
            Body.bloodVolume = json.getDouble("bloodVolume");
            Body.isAlcoholic = json.getBoolean("isAlcoholic");
            JSONArray drinksJSON = json.getJSONArray("drinks");
            for (int i = 0; i < drinksJSON.length(); i++) {
                JSONObject drinkJSON = drinksJSON.getJSONObject(i);
                drinks.add(new Drink(drinkJSON.getDouble("fractionAlcohol"),
                        drinkJSON.getDouble("sizeInG"), new Date(drinkJSON.getLong("timestamp"))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
