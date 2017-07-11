package com.aayush.drunk;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.aayush.drunk.Body.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    Date beginTime;
    Date endTime;
    DrinkAdapter drinkAdapter;
    RecyclerView recyclerView;
    Thread bacThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPref.getBoolean("initialized", false)) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            finish();
            startActivity(intent);
        }
        load(this);
        setDate(new Date(System.currentTimeMillis()));
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        drinkAdapter = new DrinkAdapter(beginTime, endTime);
        recyclerView.setAdapter(drinkAdapter);

        findViewById(R.id.shot).setOnClickListener(this);
        findViewById(R.id.beer).setOnClickListener(this);
        findViewById(R.id.custom).setOnClickListener(this);
        findViewById(R.id.other).setOnClickListener(this);

        bacThread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        if (((TextView) findViewById(R.id.dayText)).getText().equals("Today")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((TextView) findViewById(R.id.current_bac)).setText(
                                            new DecimalFormat("#.###").format(get_BAC()) + "%");
                                }
                            });
                        }
                        Thread.sleep(60000);
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        bacThread.start();
    }

    @Override
    public void onClick(View v) {
        Date timestamp = new Date(System.currentTimeMillis());
        switch (v.getId()) {
            case R.id.custom:
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                if (sharedPref.getString("customType", "").equals("")) {
                    Intent intent = new Intent(this, OtherActivity.class);
                    intent.putExtra("creating custom", true);
                    startActivity(intent);
                } else {
                    addDrink(new Drink(sharedPref.getString("customType", "this shouldn't show up"),
                            sharedPref.getFloat("customFractionAlcohol", 0),
                            sharedPref.getFloat("customSizeInOz", 0),
                            new Date(System.currentTimeMillis())), this);
                }
                break;
            case R.id.other:
                Log.d("d", "other pressed");
                startActivity(new Intent(this, OtherActivity.class));
                break;
            default:
                switch(v.getId()) {
                    case R.id.shot:
                        addDrink(new Drink(Drink.SHOT, timestamp), this);
                        break;
                    case R.id.beer:
                        addDrink(new Drink(Drink.BEER, timestamp), this);
                        break;
                }
        }
        if (timestamp.after(beginTime) && timestamp.before(endTime)) {
            updateDrinks();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ((Button) findViewById(R.id.custom)).setText(sharedPref.getString("customType", "set shortcut"));
        updateDrinks();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.chooseDate:
                final Calendar cal = new GregorianCalendar();
                cal.setTime(beginTime);
                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                cal.set(Calendar.YEAR, year);
                                cal.set(Calendar.MONTH, monthOfYear);
                                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                cal.set(Calendar.HOUR_OF_DAY, 11);
                                setDate(cal.getTime());
                                String str = DateFormat.getDateInstance().format(cal.getTime());
                                if (str.equals(DateFormat.getDateInstance().format(new Date(System.currentTimeMillis())))) {
                                    str = "Today";
                                } else if (str.equals(DateFormat.getDateInstance().format(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)))) {
                                    str = "Yesterday";
                                }
                                ((TextView) findViewById(R.id.dayText)).setText(str);
                                drinkAdapter = new DrinkAdapter(beginTime, endTime);
                                recyclerView.setAdapter(drinkAdapter);
                                ((TextView) findViewById(R.id.textView)).setText(
                                        drinksOverview(beginTime, endTime));
                            }
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                return true;
            case R.id.settings:
                return true;
            case R.id.info:
                return true;
            case R.id.shortcut:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateDrinks() {
        ((TextView) findViewById(R.id.textView)).setText(drinksOverview(beginTime, endTime));
        drinkAdapter = new DrinkAdapter(beginTime, endTime);
        recyclerView.setAdapter(drinkAdapter);
        bacThread.interrupt();
        bacThread.start();
    }

    private void setDate(Date dateToSet) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(new Date(dateToSet.getTime() - 3 * 3600 * 1000 - 31 * 60 * 1000));
        //if the time were 3:30, it is now 11:59 the prev day
        cal.set(Calendar.HOUR_OF_DAY, 3);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        beginTime = cal.getTime();
        endTime = new Date(beginTime.getTime() + 24 * 60 * 60 * 1000);
    }
}
