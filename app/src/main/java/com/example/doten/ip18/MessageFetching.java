package com.example.doten.ip18;

import android.os.Handler;
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
import io.dronefleet.mavlink.common.Heartbeat;
import io.dronefleet.mavlink.common.MavAutopilot;
import io.dronefleet.mavlink.signing.SigningConfiguration;
import io.dronefleet.mavlink.standard.StandardDialect;

public class MessageFetching extends Thread {

    private final Handler handler;
    private final FullscreenActivity  mainActivity;

    public MessageFetching(FullscreenActivity activity, Handler handler) {
        this.handler        = handler;
        this.mainActivity   = activity;
    }

    @Override
    public void run() {

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

                if(message.getPayload() instanceof Heartbeat) {
                    final SensorData sensorData = new SensorData(SensorData.MessageType.HeartBeat, "TEST");

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mainActivity.logSensorData(sensorData);
                        }
                    });
                }
            }

        } catch (UnknownHostException e1) {
            Log.d("TEST", "UnknownHostException: " + e1.getMessage());
        } catch (IOException e2) {
            Log.d("TEST", "IOException: " + e2.getMessage());
        } catch (NoSuchAlgorithmException e3) {
            Log.d("TEST", "NoSuchAlgorithmException: " + e3.getMessage());
        }

    }
}
