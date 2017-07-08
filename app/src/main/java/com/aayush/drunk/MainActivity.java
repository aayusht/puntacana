package com.aayush.drunk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.aayush.drunk.Body.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("should say true", sharedPref.getBoolean("initialized", false) ? "true" : "false");
        if (!sharedPref.getBoolean("initialized", false)) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            finish();
            startActivity(intent);
        }
        load(this);
        findViewById(R.id.shot).setOnClickListener(this);
        findViewById(R.id.beer).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Date timestamp = new Date(System.currentTimeMillis());
        switch(v.getId()) {
            case R.id.shot:
                addDrink(new Drink(Drink.SHOT, timestamp), this);
                break;
            case R.id.beer:
                addDrink(new Drink(Drink.BEER, timestamp), this);
                break;
        }
        ((TextView) findViewById(R.id.current_bac)).setText(new DecimalFormat("#.###").format(get_BAC()) + "%");
        Calendar cal = new GregorianCalendar();
        cal.setTime(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        ((TextView) findViewById(R.id.textView)).setText(drinksToString(cal.getTime(), timestamp));
    }
}
