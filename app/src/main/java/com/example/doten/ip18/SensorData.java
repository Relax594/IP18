package com.example.doten.ip18;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SensorData extends AppCompatActivity {

    public static enum MyTextView {
        TextViewRemainingTime (R.id.remainingtime),
        TextViewAltitude (R.id.altitude),
        TextViewTemperature (R.id.temperature),
        ;

        public final int id;

        MyTextView(int id) {this.id = id;}
    }

    public static enum MessageType {
        HeartBeat (0, MyTextView.TextViewRemainingTime), // TODO: TextView o.ä. für HeartBeat
        RemainingTime (1, MyTextView.TextViewRemainingTime),
        Altitude (2, MyTextView.TextViewAltitude),
        Temperature (3, MyTextView.TextViewTemperature),
        ;

        private final int id;
        public  final MyTextView myTextView;

        MessageType(int id, MyTextView myTextView) {
            this.id             = id;
            this.myTextView     = myTextView;
        }
    }

    public  MessageType type;
    private String      content;
    private Date        receiptTime;

    public SensorData(MessageType type, String content) {
        this.type           = type;
        this.content        = content;
        this.receiptTime    = new Date();
    }

    @Override
    public String toString(){
        return "Type: " + this.type.id + ", Time: " + this.receiptTime.toString() + ", Content: " + this.content;
    }

    public String getContent() {
        return this.content;
    }
}
