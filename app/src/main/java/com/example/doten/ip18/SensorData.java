package com.example.doten.ip18;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SensorData extends AppCompatActivity {

    public SensorData() {}

    public static String UpdateRemainingFlightTime() {

        // TODO: Get real value from backend

        return "";
    }

    public static String UpdateAltitude() {

        // TODO: Get real value from backend

        Random r = new Random();
        int randomAltitude = r.nextInt(35);

        return randomAltitude + "m";
    }

    public static String UpdateTemperature() {

        // TODO: Get real value from backend

        Random r = new Random();
        int randomTemperature = r.nextInt(30);

        return randomTemperature + " °C";
    }
}
