package com.aayush.drunk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

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
        Body.load(this);

    }
}
