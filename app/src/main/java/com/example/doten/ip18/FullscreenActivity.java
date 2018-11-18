package com.example.doten.ip18;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.example.doten.ip18.SensorData.UpdateAltitude;
import static com.example.doten.ip18.SensorData.UpdateRemainingFlightTime;
import static com.example.doten.ip18.SensorData.UpdateTemperature;


public class FullscreenActivity extends AppCompatActivity {

    // Get Controls from Layout
    TextView remainingFlightTimeTextView;
    TextView altitudeTextView;
    TextView temperaturTextView;
    SharedPreferences sPrefs;

    // Set Timer to reload data every second
    private Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fullscreen);

        remainingFlightTimeTextView = (TextView)findViewById(R.id.remainingtime);
        altitudeTextView = (TextView)findViewById(R.id.altitude);
        temperaturTextView = (TextView)findViewById(R.id.temperature);
        sPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Update Data with Timer
        handler.postDelayed(runnable, 100);
        ValuateRemainingFlightTime();
    }

    private void ValuateRemainingFlightTime() {

        new CountDownTimer(300000, 1000) {

            public void onTick(long millisUntilFinished) {
                remainingFlightTimeTextView.setText(""+String.format("%d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));


            }

            public void onFinish() {
                remainingFlightTimeTextView.setText("Time expired");
            }
        }.start();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            // Update Data from backend
            altitudeTextView.setText(UpdateAltitude());
            temperaturTextView.setText(UpdateTemperature());
            //remainingFlightTimeTextView.setText(UpdateRemainingFlightTime());

            // restart timer
            handler.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        ReloadOnSettingsChanged();
    }

    public void ReloadOnSettingsChanged() {
        // Read Settings by Key
        String prefFlightTimeKey = getString(R.string.pref_flightTime_key);
        String prefAltitudeKey = getString(R.string.pref_altitude_key);
        String prefTemperatureKey = getString(R.string.pref_temperature_key);

        if (sPrefs.getBoolean(prefFlightTimeKey, true)) remainingFlightTimeTextView.setVisibility(View.VISIBLE);
        else remainingFlightTimeTextView.setVisibility(View.GONE);

        if (sPrefs.getBoolean(prefAltitudeKey, true)) altitudeTextView.setVisibility(View.VISIBLE);
        else altitudeTextView.setVisibility(View.GONE);

        if (sPrefs.getBoolean(prefTemperatureKey, true)) temperaturTextView.setVisibility(View.VISIBLE);
        else temperaturTextView.setVisibility(View.GONE);
    }


    public void ButtonSettings_onClick(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }
}
