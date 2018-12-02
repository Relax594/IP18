package com.example.doten.ip18;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.example.doten.ip18.MessageFetching;
import com.example.doten.ip18.SensorData;


public class FullscreenActivity extends AppCompatActivity {

    // Get Controls from Layout
    TextView remainingFlightTimeTextView;
    TextView altitudeTextView;
    TextView temperatureTextView;
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

        remainingFlightTimeTextView = findViewById(R.id.remainingtime);
        altitudeTextView = findViewById(R.id.altitude);
        temperatureTextView = findViewById(R.id.temperature);
        sPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // create Handler and start MessageFetching
        Handler handler = new Handler();
        new Thread(new MessageFetching(this, handler)).start();
    }

    public void logSensorData(SensorData sensorData) {
        if (sensorData.type == SensorData.MessageType.HeartBeat && showHeartbeat) {
            // show heartbeat message
            Toast.makeText(getApplicationContext(), "Heartbeat received", Toast.LENGTH_LONG).show();
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
        String prefFlightTimeKey = getString(R.string.pref_flightTime_key);
        String prefAltitudeKey = getString(R.string.pref_altitude_key);
        String prefTemperatureKey = getString(R.string.pref_temperature_key);
        String prefVibrationKey = getString(R.string.pref_vibrate_key);

        if (sPrefs.getBoolean(prefFlightTimeKey, true)) remainingFlightTimeTextView.setVisibility(View.VISIBLE);
        else remainingFlightTimeTextView.setVisibility(View.GONE);

        if (sPrefs.getBoolean(prefAltitudeKey, true)) altitudeTextView.setVisibility(View.VISIBLE);
        else altitudeTextView.setVisibility(View.GONE);

        if (sPrefs.getBoolean(prefTemperatureKey, true)) temperatureTextView.setVisibility(View.VISIBLE);
        else temperatureTextView.setVisibility(View.GONE);

        doVibrate = sPrefs.getBoolean(prefVibrationKey, true);
    }


    public void ButtonSettings_onClick(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }
}
