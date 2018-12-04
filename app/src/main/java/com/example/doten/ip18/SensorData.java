package com.example.doten.ip18;

import android.support.v7.app.AppCompatActivity;

import java.util.Date;

public class SensorData extends AppCompatActivity {

    public enum MyTextView {
        Nothing (0),
        TextViewRemainingBatt (R.id.remainingbatt),
        TextViewRemainingTime (R.id.remainingtime),
        TextViewAltitude (R.id.altitude),
        TextViewTemperature (R.id.temperature),
        ;

        public final int id;

        MyTextView(int id) {this.id = id;}
    }

    public enum MessageType {
        HeartBeat (0, MyTextView.Nothing),
        RemainingBatt (1, MyTextView.TextViewAltitude.TextViewRemainingBatt),
        RemainingTime (2, MyTextView.TextViewRemainingTime),
        Altitude (3, MyTextView.TextViewAltitude),
        Temperature (4, MyTextView.TextViewTemperature),
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
