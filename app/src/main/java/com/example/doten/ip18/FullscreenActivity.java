package com.example.doten.ip18;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class FullscreenActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fullscreen);
        SetRemainingTime();
    }

    public void SetRemainingTime() {
        new CountDownTimer(300000, 1000) {
            TextView remainingTimeTextView = (TextView)findViewById(R.id.remainingtime);

            public void onTick(long millisUntilFinished) {
                remainingTimeTextView.setText(""+String.format("%d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
                remainingTimeTextView.setText("Time expired");
            }
        }.start();
    }



    public void ButtonSettings_onClick(View view) {

        Intent intent= new Intent(this,SettingsActivity.class);
        startActivity(intent);
    }
}
