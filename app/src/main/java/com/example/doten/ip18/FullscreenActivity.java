package com.example.doten.ip18;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
        temperatureTextView = (TextView)findViewById(R.id.temperature);
        sPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Erstelle Handler und starte MessageFetching
        Handler handler = new Handler();
        new Thread(new MessageFetching(this, handler)).start();
    }

    public void logSensorData(SensorData sensorData) {
        if (sensorData.type == SensorData.MessageType.HeartBeat) {
            Toast.makeText(getApplicationContext(), "Heartbeat received", Toast.LENGTH_LONG).show();
        }

        Log.d("TEST", "content: " + sensorData.getContent());
        Log.d("TEST", "target: " + sensorData.type.myTextView.id);

        TextView textViewToUpdate = (TextView)findViewById(sensorData.type.myTextView.id);

        if (textViewToUpdate != null) {
            textViewToUpdate.setText(sensorData.getContent());
        }
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

        if (sPrefs.getBoolean(prefFlightTimeKey, true)) remainingFlightTimeTextView.setVisibility(View.VISIBLE);
        else remainingFlightTimeTextView.setVisibility(View.GONE);

        if (sPrefs.getBoolean(prefAltitudeKey, true)) altitudeTextView.setVisibility(View.VISIBLE);
        else altitudeTextView.setVisibility(View.GONE);

        if (sPrefs.getBoolean(prefTemperatureKey, true)) temperatureTextView.setVisibility(View.VISIBLE);
        else temperatureTextView.setVisibility(View.GONE);
    }


    public void ButtonSettings_onClick(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }
}
