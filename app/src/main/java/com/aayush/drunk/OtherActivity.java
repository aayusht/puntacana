package com.aayush.drunk;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class OtherActivity extends AppCompatActivity implements View.OnClickListener{
    private boolean creatingCustom = false;
    private TextView dateText, timeText;
    private Date timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        creatingCustom = getIntent().getBooleanExtra("creating custom", false);
        dateText = (TextView) findViewById(R.id.dateText);
        timeText = (TextView) findViewById(R.id.timeText);
        if (creatingCustom) {
            dateText.setVisibility(View.GONE);
            timeText.setVisibility(View.GONE);
            findViewById(R.id.time).setVisibility(View.GONE);
            findViewById(R.id.date).setVisibility(View.GONE);
        } else {
            timestamp = new Date(System.currentTimeMillis());
            updateTimeViews();
        }
        findViewById(R.id.time).setOnClickListener(this);
        findViewById(R.id.date).setOnClickListener(this);
        findViewById(R.id.submit).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.time:
                final Calendar cal = new GregorianCalendar();
                cal.setTime(timestamp);
                TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                cal.set(Calendar.MINUTE, minute);
                                timestamp = (Date) cal.getTime().clone();
                                updateTimeViews();
                            }
                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
                timePickerDialog.show();
                break;
            case R.id.date:
                final Calendar cal2 = new GregorianCalendar();
                cal2.setTime(timestamp);
                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                cal2.set(Calendar.YEAR, year);
                                cal2.set(Calendar.MONTH, monthOfYear);
                                cal2.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                timestamp = (Date) cal2.getTime().clone();
                                updateTimeViews();
                            }
                        }, cal2.get(Calendar.YEAR), cal2.get(Calendar.MONTH), cal2.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                break;
            case R.id.submit:
                if (creatingCustom) {
                    createCustom();
                } else {
                    String type = ((EditText) findViewById(R.id.drinkName)).getText().toString();
                    double fractionAlcohol = Double.parseDouble(((EditText) findViewById(R.id.ABV))
                            .getText().toString()) / 100;
                    if (((Spinner) findViewById(R.id.ABVspinner)).getSelectedItem().toString().equals("proof")) {
                        fractionAlcohol /= 2;
                    }
                    double sizeInOz = Double.parseDouble(((EditText) findViewById(R.id.drinkSize))
                            .getText().toString());
                    if (((Spinner) findViewById(R.id.sizeSpinner)).getSelectedItem().toString().equals("mL")) {
                        sizeInOz = Utils.gToOz(sizeInOz);
                    }
                    Body.addDrink(new Drink(type, fractionAlcohol, sizeInOz, timestamp), this);
                }
                finish();
        }
    }

    private void createCustom() {
        String customType = ((EditText) findViewById(R.id.drinkName)).getText().toString();
        float customFractionAlcohol = Float.parseFloat(((EditText) findViewById(R.id.ABV))
                .getText().toString()) / 100;
        if (((Spinner) findViewById(R.id.ABVspinner)).getSelectedItem().toString().equals("proof")) {
            customFractionAlcohol /= 2;
        }
        float customSizeInOz = Float.parseFloat(((EditText) findViewById(R.id.drinkSize))
                .getText().toString());
        if (((Spinner) findViewById(R.id.sizeSpinner)).getSelectedItem().toString().equals("mL")) {
            customSizeInOz = (float) Utils.gToOz(customSizeInOz);
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        //TODO put these variables in Utils or something
        editor.putString("customType", customType);
        editor.putFloat("customFractionAlcohol", customFractionAlcohol);
        editor.putFloat("customSizeInOz", customSizeInOz);
        editor.commit();
    }

    private void updateTimeViews() {
        dateText.setText(DateFormat.getDateInstance().format(timestamp));
        timeText.setText(DateFormat.getTimeInstance().format(timestamp));
    }
}
