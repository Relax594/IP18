package com.example.doten.ip18;

import android.os.Handler;
import android.os.Looper;
import android.se.omapi.Session;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.dronefleet.mavlink.MavlinkConnection;
import io.dronefleet.mavlink.MavlinkMessage;
import io.dronefleet.mavlink.ardupilotmega.ArdupilotmegaDialect;
import io.dronefleet.mavlink.common.BatteryStatus;
import io.dronefleet.mavlink.common.GlobalPositionInt;
import io.dronefleet.mavlink.common.Heartbeat;
import io.dronefleet.mavlink.common.MavAutopilot;
import io.dronefleet.mavlink.common.SysStatus;
import io.dronefleet.mavlink.signing.SigningConfiguration;
import io.dronefleet.mavlink.standard.StandardDialect;

public class MessageFetching extends Thread {

    private final Handler handler;
    private final FullscreenActivity activity;

    public MessageFetching(FullscreenActivity activity, Handler handler) {
        this.handler    = handler;
        this.activity   = activity;
    }

    @Override
    public void run() {

        Looper.prepare();

        try{
            Socket socket = new Socket(Config.IP, Config.PORT);

            Log.d("TEST", "Sucess: socket created.");

            MavlinkConnection connection = MavlinkConnection.builder(
                    socket.getInputStream(),
                    socket.getOutputStream()
            )
                    .dialect(MavAutopilot.MAV_AUTOPILOT_GENERIC, new StandardDialect())
                    .dialect(MavAutopilot.MAV_AUTOPILOT_ARDUPILOTMEGA, new ArdupilotmegaDialect())
                    .signing(new SigningConfiguration(
                            0,
                            1,
                            MessageDigest.getInstance("SHA-256")
                                    .digest("my secret key".getBytes(StandardCharsets.UTF_8))
                    ))
                    .build();

            MavlinkMessage message;

            while ((message = connection.next()) != null) {

                // Log.d("TEST", "Received MavlinkMessage: " + message.getPayload().toString());

                final SensorData sensorData;

                if(message.getPayload() instanceof Heartbeat) {
                    // Log.d("TEST", "Received HeartBeat");
                    sensorData = new SensorData(SensorData.MessageType.HeartBeat, "");
                } else if(message.getPayload() instanceof SysStatus) {
                    // Log.d("TEST", "Received SysStatus");
                    MavlinkMessage<SysStatus> m = (MavlinkMessage<SysStatus>)message;
                    int batteryRemaining        = m.getPayload().batteryRemaining();
                    String content;
                    if(batteryRemaining == -1) {
                        content = "N/A";
                    } else {
                        content              = String.valueOf(m.getPayload().batteryRemaining()) + "%";
                    }
                    sensorData = new SensorData(SensorData.MessageType.RemainingBatt, content);
                } else if(message.getPayload() instanceof BatteryStatus) {
                    // Log.d("TEST", "Received BatteryStatus");
                    MavlinkMessage<BatteryStatus> m = (MavlinkMessage<BatteryStatus>)message;
                    int timeRemaining               = m.getPayload().timeRemaining();
                    String content                  = "";
                    if(timeRemaining == 0){
                        content = "N/A";
                    } else {
                        int minutes = timeRemaining / 60;
                        int seconds = timeRemaining % 60;
                        content = String.format("%d:%02d", minutes, seconds);
                    }
                    sensorData = new SensorData(SensorData.MessageType.RemainingTime, content);
                } else if(message.getPayload() instanceof GlobalPositionInt) {
                    // Log.d("TEST", "Received GlobalPositionInt");
                    MavlinkMessage<GlobalPositionInt> m = (MavlinkMessage<GlobalPositionInt>)message;
                    int relativeAltitudeInMm            = m.getPayload().relativeAlt();
                    float relativeAltitudeInMeters      = ((float) relativeAltitudeInMm) / 1000.0f;
                    String content                      = String.valueOf(relativeAltitudeInMeters) + "m";
                    sensorData = new SensorData(SensorData.MessageType.Altitude, content);
                } else {
                    continue;
                }

                handler.post(new SendSensorDataToUI(sensorData, activity));

            }

        } catch (UnknownHostException e1) {
            Log.d("TEST", "UnknownHostException: " + e1.getMessage());
        } catch (IOException e2) {
            Log.d("TEST", "IOException: " + e2.getMessage());
        } catch (NoSuchAlgorithmException e3) {
            Log.d("TEST", "NoSuchAlgorithmException: " + e3.getMessage());
        }

    }

    private class SendSensorDataToUI implements Runnable {

        private SensorData          sensorData;
        private FullscreenActivity  activity;

        public SendSensorDataToUI(SensorData sensorData, FullscreenActivity activity) {
            this.sensorData = sensorData;
            this.activity   = activity;
        }

        @Override
        public void run() {
            activity.logSensorData(sensorData);
        }
    }
}
