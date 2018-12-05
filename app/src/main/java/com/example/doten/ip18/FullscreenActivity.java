package com.example.doten.ip18;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class FullscreenActivity extends AppCompatActivity {

    // Get Controls from Layout
    TextView remainingFlightTimeTextView;
    TextView remainingBatteryTextView;
    TextView altitudeTextView;
    TextView temperatureTextView;
    ImageView batteryImageView;
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
        batteryImageView            = findViewById(R.id.batteryImage);
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

        // refresh connection state to drone every 5 seconds
        refreshConnectionState();

        // create Handler and start MessageFetching
        Handler handler = new Handler();
        new Thread(new MessageFetching(this, handler)).start();
    }

    private void refreshConnectionState() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                showHeartbeat = true;
                droneView.setBackground(ContextCompat.getDrawable(FullscreenActivity.this, R.drawable.view_disconnected));
            }
        }, 0, 5000);
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

        // No check for remaining time needed (battery state does the same basically)
        if (dataType == SensorData.MessageType.RemainingTime) return;

        // remove % from string (for battery state)
        newValue = newValue.replace("%", "");

        // remove m from string (for altitude)
        newValue = newValue.replace("m", "");

        int result = Integer.parseInt(newValue);

        if (dataType == SensorData.MessageType.Temperature) {
            if (result > 50){
                PlayNotification("The Temperature is becoming too high (more then 50°C)");
            }
        } else if (dataType == SensorData.MessageType.Altitude) {
            if(result > 120){
                PlayNotification("Becareful, Drone is going out of control.");
            }
        } else if (dataType == SensorData.MessageType.RemainingBatt) {
            if (result < 10) {
                PlayNotification("Battery is low (< 10% remaining).");
                batteryImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.battery_empty));
            }
            else if (result < 20) {
                PlayNotification("Battery is running low (< 20% remaining).");
                batteryImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.battery_low));
            } else if (result < 50) {
                batteryImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.battery_half));
            }
        }
    }

    public void PlayNotification(String warningMessage) {
        //Define Notification Manager
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);

        Uri soundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Notification.Builder mNotification = new Notification.Builder(this)
                .setContentTitle("Warning!")
                .setSmallIcon(R.drawable.alert)
                .setContentText(warningMessage)
                .setSound(soundURI);

        if (doVibrate) {
            mNotification.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        }

        notificationManager.notify(0, mNotification.build());
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
