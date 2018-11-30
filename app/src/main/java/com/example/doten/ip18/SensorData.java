package com.example.doten.ip18;

import android.support.v7.app.AppCompatActivity;

import java.util.Date;

public class SensorData extends AppCompatActivity {

    public enum MyTextView {
        Nothing (0),
        TextViewRemainingTime (R.id.remainingtime),
        TextViewAltitude (R.id.altitude),
        TextViewTemperature (R.id.temperature),
        ;

        public final int id;

        MyTextView(int id) {this.id = id;}
    }

    public enum MessageType {
        HeartBeat (0, MyTextView.Nothing),
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
