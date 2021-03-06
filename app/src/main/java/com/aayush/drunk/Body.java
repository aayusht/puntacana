package com.aayush.drunk;

/**
 * Created by account on 7/4/17.
 */

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.aayush.drunk.Drink.*;
import static com.aayush.drunk.Utils.gToOz;

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
    final static int MAX_DRINKS = 10000;
    final static String FILENAME = "bodydata.json";

    static double r = 0;
    static double weight = 0; //in kilos
    static double bloodVolume = 0; // in mL
    static boolean isAlcoholic = false;

    static ArrayList<Drink> drinks = new ArrayList<>();

    static double get_BAC() {
        if (drinks.size() == 0) { return 0; }
        int i = Math.min(drinks.size() - 1, 69);
        double totalAlcohol = drinks.get(i).currentBAC();
        long prevTime = drinks.get(i).timestamp.getTime();
        double eliminationRate = Body.isAlcoholic ? .025 : .015; //TODO changes if you're hungry tho
        while (i > 0) {
            Drink nextDrink = drinks.get(--i);
            double hoursElapsed = (nextDrink.timestamp.getTime() - prevTime) / 3600000.;
            double alc = nextDrink.currentBAC() - eliminationRate * hoursElapsed;
            totalAlcohol += Math.max(alc, 0);
            prevTime = nextDrink.timestamp.getTime();
        }
        return Math.max(totalAlcohol - ((System.currentTimeMillis() - prevTime) / 3600000.) * eliminationRate, 0);
    }

    static double peak_BAC(Date beginTime, Date endTime) {
        int startIndex = getDrinkIndex(endTime); //Not a mistake!
        if (startIndex == -1) {
            return 0.;
        }
        startIndex = Math.max(startIndex - 20, 0);
        int endIndex = getDrinkIndex(beginTime);
        endIndex = endIndex < 0 ? drinks.size() - 1 : endIndex;
        int i = endIndex;
        double totalAlcohol = drinks.get(i).currentBAC();
        double peakAlcohol = totalAlcohol;
        long prevTime = drinks.get(i).timestamp.getTime();
        double eliminationRate = Body.isAlcoholic ? .025 : .015; //TODO changes if you're hungry tho
        while (i > startIndex) {
            Drink nextDrink = drinks.get(--i);
            double hoursElapsed = (nextDrink.timestamp.getTime() - prevTime) / 3600000.;
            double alc = nextDrink.currentBAC() - eliminationRate * hoursElapsed;
            totalAlcohol += Math.max(alc, 0);
            if (nextDrink.timestamp.after(beginTime)) {
                peakAlcohol = Math.max(totalAlcohol, peakAlcohol);
            }
            prevTime = nextDrink.timestamp.getTime();
        }
        return peakAlcohol;
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
                drinkJSON.put("type", drink.type);
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
                drinks.add(new Drink(drinkJSON.getString("type"),
                        drinkJSON.getDouble("fractionAlcohol"),
                        gToOz(drinkJSON.getDouble("sizeInG")),
                        new Date(drinkJSON.getLong("timestamp"))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts drink into arraylist chronologically (most recent first)
     * @param drink Drink to be added
     */
    static void addDrink(Drink drink, Context context) {
        int index = getDrinkIndex(drink.timestamp);
        if (drinks.size() == MAX_DRINKS) {
            drinks.remove(drinks.size() - 1);
        }
        if (index == -1) {
            drinks.add(drink);
            return;
        }
        drinks.add(index, drink);
        save(context);
    }

    /**
     * Gets index of drink immediately after (chronologically before) given time
     * @param time
     * @return index of drink in drinks, -1 if none
     */
    static int getDrinkIndex(Date time) {
        if (drinks.size() == 0 || drinks.get(drinks.size() - 1).timestamp.after(time)) {
            Log.d("msg", "no drinks before this time");
            return -1;
        } else if (drinks.get(0).timestamp.before(time) || drinks.get(0).timestamp.equals(time)){
            return 0;
        } else {
            int beginIndex = 0;
            int endIndex = drinks.size();
            int mid;
            while (endIndex - beginIndex > 1) {
                mid = (endIndex - beginIndex) / 2;
                if (drinks.get(mid).timestamp.before(time)) {
                    endIndex = mid;
                } else if (drinks.get(mid).timestamp.after(time)) {
                    beginIndex = mid;
                } else {
                    return mid;
                }
            }
            return endIndex;
        }
    }

    static String drinksOverview(Date startTime, Date endTime) {
        int startIndex = getDrinkIndex(endTime); //Not a mistake!
        if (startIndex == -1) {
            return "No drinks!";
        }
        int endIndex = getDrinkIndex(startTime);
        endIndex = endIndex == -1 ? drinks.size() - 1 : endIndex - 1;
        Log.d("hmm", "" + startTime + " " + startIndex + " " + endTime + " " + endIndex);
        String other = "";
        HashMap<String, Integer> otherDrinks = new HashMap<>();
        int shots = 0;
        int beers = 0;
        int malts = 0;
        int wines = 0;
        int fortifieds = 0;
        int liqueurs = 0;
        for (int i = startIndex; i <= endIndex; i++) {
            Drink drink = drinks.get(i);
            switch(drink.type) {
                case BEER:
                    beers++;
                    break;
                case MALT:
                    malts++;
                    break;
                case WINE:
                    wines++;
                    break;
                case FORTIFIED_WINE:
                    fortifieds++;
                    break;
                case LIQUEUR:
                    liqueurs++;
                    break;
                case SHOT:
                    shots++;
                    break;
                default:
                    String drinkString = drink.toString();
                    if (otherDrinks.get(drinkString) == null) {
                        otherDrinks.put(drinkString, 1);
                    } else {
                        otherDrinks.put(drinkString, otherDrinks.get(drinkString) + 1);
                    }
            }
        }
        String str = "";
        if (shots > 0) {
            if (shots == 1) { str += "a shot, "; }
            else { str += shots + " shots, "; }
        }
        if (beers > 0) {
            if (beers == 1) { str += "a beer, "; }
            else { str += beers + " beers, "; }
        }
        if (malts > 0) {
            if (malts == 1) { str += "a malt, "; }
            else { str += malts + " malts, "; }
        }
        if (wines > 0) {
            if (wines == 1) { str += "a glass of wine, "; }
            else { str += wines + " glasses of wine, "; }
        }
        if (fortifieds > 0) {
            if (fortifieds == 1) { str += "a glass of fortified wine, "; }
            else { str += fortifieds + " glasses of fortified wine, "; }
        }
        if (liqueurs > 0) {
            if (liqueurs == 1) { str += "a glass of liqueur, "; }
            else { str += liqueurs + " glasses of liqueur, "; }
        }
        for (Map.Entry<String, Integer> entry : otherDrinks.entrySet()) {
            other += entry.getValue() + " " + entry.getKey() + ", ";
        }
        if (!(str.equals("") && other.equals(""))) {
            other += "and a peak BAC of "
                    + new DecimalFormat("#.###").format(peak_BAC(startTime, endTime)) + "%";
        } else {
            return "Sober!";
        }
        if (str.charAt(0) == 'a') {
            str = "A" + str.substring(1);
        }
        return str + other;
    }
}
