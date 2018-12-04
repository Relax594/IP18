package com.example.doten.ip18;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


public class FullscreenActivity extends AppCompatActivity {

    // Get Controls from Layout
    TextView remainingFlightTimeTextView;
    TextView remainingBatteryTextView;
    TextView altitudeTextView;
    TextView temperatureTextView;
    View droneView;
    SharedPreferences sPrefs;

    boolean showHeartbeat = true;
    boolean doVibrate = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fullscreen);

        remainingBatteryTextView    = findViewById(R.id.remainingbatt);
        remainingFlightTimeTextView = findViewById(R.id.remainingtime);
        altitudeTextView            = findViewById(R.id.altitude);
        temperatureTextView         = findViewById(R.id.temperature);
        droneView                   = findViewById(R.id.droneActive);
        sPrefs                      = PreferenceManager.getDefaultSharedPreferences(this);

        View settings = findViewById(R.id.settingsImage);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FullscreenActivity.this, SettingsActivity.class));
            }
        });

        // create Handler and start MessageFetching
        Handler handler = new Handler();
        new Thread(new MessageFetching(this, handler)).start();
    }

    public void logSensorData(SensorData sensorData) {
        if (sensorData.type == SensorData.MessageType.HeartBeat && showHeartbeat) {
            // show heartbeat message
            droneView.setBackground(ContextCompat.getDrawable(this, R.drawable.view_connected));
            showHeartbeat = false;
        } else {
            CheckValue(sensorData.getContent(), sensorData.type);

            // update textview based on data
            TextView textViewToUpdate = findViewById(sensorData.type.myTextView.id);

            if (textViewToUpdate != null) textViewToUpdate.setText(sensorData.getContent());
        }
    }

    public void CheckValue(String newValue, SensorData.MessageType dataType) {
        if (dataType == SensorData.MessageType.Temperature) {

        } else if (dataType == SensorData.MessageType.Altitude) {

        }

        // TODO: check other dataType with value
        // TODO: PlayNotification when value gets over limit
    }

    public void PlayNotification() {
        // TODO: ADD NotificationManager with Settings
    }

    @Override
    protected void onResume() {
        super.onResume();
        ReloadOnSettingsChanged();
    }

    public void ReloadOnSettingsChanged() {
        // Read Settings by Key
        String prefBatteryKey = getString(R.string.pref_battery_key);
        String prefFlightTimeKey = getString(R.string.pref_flightTime_key);
        String prefAltitudeKey = getString(R.string.pref_altitude_key);
        String prefTemperatureKey = getString(R.string.pref_temperature_key);
        String prefVibrationKey = getString(R.string.pref_vibrate_key);

        // Battery State
        if (sPrefs.getBoolean(prefBatteryKey, true)) {
            remainingBatteryTextView.setVisibility(View.VISIBLE);
            findViewById(R.id.batteryImage).setVisibility(View.VISIBLE);
        }
        else {
            remainingBatteryTextView.setVisibility(View.GONE);
            findViewById(R.id.batteryImage).setVisibility(View.GONE);
        }

        // Remaining Flight TIme
        if (sPrefs.getBoolean(prefFlightTimeKey, true)) {
            remainingFlightTimeTextView.setVisibility(View.VISIBLE);
            findViewById(R.id.remainingTimeImage).setVisibility(View.VISIBLE);
        }
        else {
            remainingFlightTimeTextView.setVisibility(View.GONE);
            findViewById(R.id.remainingTimeImage).setVisibility(View.GONE);
        }

        // Altitude
        if (sPrefs.getBoolean(prefAltitudeKey, true)) {
            altitudeTextView.setVisibility(View.VISIBLE);
            findViewById(R.id.altitudeImage).setVisibility(View.VISIBLE);
        }
        else {
            altitudeTextView.setVisibility(View.GONE);
            findViewById(R.id.altitudeImage).setVisibility(View.GONE);
        }

        // Temperature
        if (sPrefs.getBoolean(prefTemperatureKey, true)) {
            temperatureTextView.setVisibility(View.VISIBLE);
            findViewById(R.id.temperatureImage).setVisibility(View.VISIBLE);
        }
        else {
            temperatureTextView.setVisibility(View.GONE);
            findViewById(R.id.temperatureImage).setVisibility(View.GONE);
        }

        doVibrate = sPrefs.getBoolean(prefVibrationKey, true);
    }
}
