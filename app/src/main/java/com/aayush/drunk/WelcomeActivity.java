package com.aayush.drunk;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener{
    private Double weight = null;
    private Double height = null;
    private Boolean isAlcoholic = null;
    private Boolean isMale = null;
    private Boolean isWeightMetric = null;
    private Boolean isHeightMetric = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this,
                R.array.weight_units, R.layout.spinner_item);

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        ((Spinner) findViewById(R.id.weight_spinner)).setAdapter(adapter);

        ArrayAdapter adapter2 = ArrayAdapter.createFromResource(this,
                R.array.height_units, R.layout.spinner_item);

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        ((Spinner) findViewById(R.id.height_spinner)).setAdapter(adapter2);

        findViewById(R.id.continue_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case(R.id.continue_button):
                isMale = ((RadioButton) findViewById(R.id.male)).isChecked();
                isAlcoholic = ((CheckBox) findViewById(R.id.checkBox)).isChecked();
                weight = Double.parseDouble(((EditText) findViewById(R.id.weight)).getText().toString());
                height = Double.parseDouble(((EditText) findViewById(R.id.height)).getText().toString());
                isWeightMetric = ((Spinner) findViewById(R.id.weight_spinner)).getSelectedItem()
                        .toString().equals("kgs");
                isHeightMetric = ((Spinner) findViewById(R.id.height_spinner)).getSelectedItem()
                        .toString().equals("cm");
                if(weight == null || height == null || isAlcoholic == null || isMale == null ||
                        isWeightMetric == null || isHeightMetric == null) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    Body.initialize(weight, isMale, height, isAlcoholic, isWeightMetric, isHeightMetric);
                    Body.save(this);
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("initialized", true);
                    editor.commit();
                    startActivity(new Intent(this, MainActivity.class));
                }
        }
    }
}
