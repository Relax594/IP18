package com.example.doten.ip18;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.serenegiant.usb.Size;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class FullscreenActivity extends AppCompatActivity implements CameraDialog.CameraDialogParent {

    BaseActivity baseActivity;

    // Notification IDs
    public enum Notifications {
        No (0),
        Battery (1),
        Altitude (2),
        Temperature (3),
        ;

        public final int id;

        Notifications(int id) {this.id = id;}
    }

    // Get Controls from Layout
    private TextView remainingFlightTimeTextView;
    private TextView remainingBatteryTextView;
    private TextView altitudeTextView;
    private TextView temperatureTextView;
    private ImageView batteryImageView;
    private View droneView;
    private SharedPreferences sPrefs;
    private Drawable connectedDrawable;

    private final Object mSync = new Object();

    private static final boolean DEBUG = false;	// TODO set false when production
    private static final String TAG = "FullscreenActivity";

    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    private SurfaceView mUVCCameraView;
    // for open&start / stop&close camera preview
    private ImageButton mCameraButton;
    private Surface mPreviewSurface;
    private boolean isActive, isPreview;

    //Define Notification Manager
    NotificationManager notificationManager;

    Date lastMessageReceived;
    boolean showedAltitudeWarning = false;
    boolean showedTemperatureWarning = false;
    boolean showedBatteryWarning = false;
    boolean doVibrate = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fullscreen);

        baseActivity = new BaseActivity();

        mCameraButton = (ImageButton)findViewById(R.id.camera_button);
        mCameraButton.setOnClickListener(mOnClickListener);

        mUVCCameraView = (SurfaceView)findViewById(R.id.UVCCameraTextureView1);
        mUVCCameraView.getHolder().addCallback(mSurfaceViewCallback);

        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);

        remainingBatteryTextView    = findViewById(R.id.remainingbatt);
        batteryImageView            = findViewById(R.id.batteryImage);
        remainingFlightTimeTextView = findViewById(R.id.remainingtime);
        altitudeTextView            = findViewById(R.id.altitude);
        temperatureTextView         = findViewById(R.id.temperature);
        droneView                   = findViewById(R.id.droneActive);
        sPrefs                      = PreferenceManager.getDefaultSharedPreferences(this);
        notificationManager         = (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);
        connectedDrawable           = ContextCompat.getDrawable(this, R.drawable.view_connected);
        lastMessageReceived         = new Date();

        View settings = findViewById(R.id.settingsImage);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FullscreenActivity.this, SettingsActivity.class));
            }
        });

        // refresh connection state to drone every second
        refreshConnectionState();

        // create Handler and start MessageFetching
        Handler handler = new Handler();
        new Thread(new MessageFetching(this, handler)).start();
    }

    private void refreshConnectionState() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if ((new Date().getTime() - lastMessageReceived.getTime()) > 1000) {
                            droneView.setBackground(ContextCompat.getDrawable(FullscreenActivity.this, R.drawable.view_disconnected));
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    private int GetScreenWidth() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getRealSize(size);
        return size.x;
    }

    private int GetScreenHeight() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getRealSize(size);
        return size.y;
    }

    public void logSensorData(SensorData sensorData) {

        // refresh connection state if needed
        if (droneView.getBackground() != connectedDrawable) {
            droneView.setBackground(connectedDrawable);
        }

        lastMessageReceived = new Date();

        // no need to refresh anything
        if (sensorData.type == SensorData.MessageType.HeartBeat) return;

        // check value for possible needed warning
        CheckValue(sensorData.getContent(), sensorData.type);

        // update textview based on data
        TextView textViewToUpdate = findViewById(sensorData.type.myTextView.id);

        if (textViewToUpdate != null) textViewToUpdate.setText(sensorData.getContent());
    }

    public void CheckValue(String newValue, SensorData.MessageType dataType) {

        // No check for remaining time needed (battery state does the same basically)
        if (dataType == SensorData.MessageType.RemainingTime) return;

        // No value given
        if (newValue == "N/A") return;
        
        // remove % from string (for battery state)
        newValue = newValue.replace("%", "");

        // remove m from string (for altitude)
        newValue = newValue.replace("m", "");

        float result = Float.parseFloat(newValue);

        if (dataType == SensorData.MessageType.Temperature) {
            if (result > 50 && !showedTemperatureWarning){
                showedTemperatureWarning = true;
                PlayNotification("The Temperature is becoming too high (more then 50°C)", Notifications.Temperature.id);
            } else {
                showedTemperatureWarning = false;
                CancelNotification(Notifications.Temperature.id);
            }
        } else if (dataType == SensorData.MessageType.Altitude) {
            if(result > 120 && !showedAltitudeWarning){
                showedAltitudeWarning = true;
                PlayNotification("Be careful, Drone is going out of control.", Notifications.Altitude.id);
            } else {
                showedAltitudeWarning = false;
                CancelNotification(Notifications.Altitude.id);
            }
        } else if (dataType == SensorData.MessageType.RemainingBatt) {
            if (result < 10 && !showedBatteryWarning) {
                showedBatteryWarning = true;

                PlayNotification("Battery is low (< 10% remaining).", Notifications.Battery.id);
                batteryImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.battery_empty));
            }
            else if (result < 20 && result > 10) {
                showedBatteryWarning = false;
            }
            else if (result < 20 && !showedBatteryWarning) {
                showedBatteryWarning = true;

                PlayNotification("Battery is running low (< 20% remaining).", Notifications.Battery.id);
                batteryImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.battery_low));
            } else if (result < 50) {
                batteryImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.battery_half));
            } else {
                showedBatteryWarning = false;
                CancelNotification(Notifications.Battery.id);
            }
        }
    }

    private void CancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }

    public void PlayNotification(String warningMessage, int notificationId) {

        Uri soundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Notification.Builder mNotification = new Notification.Builder(this)
                .setContentTitle("Warning!")
                .setSmallIcon(R.drawable.alert)
                .setContentText(warningMessage)
                .setAutoCancel(true)
                .setSound(soundURI);

        if (doVibrate) {
            mNotification.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        }

        notificationManager.notify(notificationId, mNotification.build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) Log.v(TAG, "onStart:");
        synchronized (mSync) {
            if (mUSBMonitor != null) {
                mUSBMonitor.register();
            }
        }
    }

    @Override
    protected void onStop() {
        if (DEBUG) Log.v(TAG, "onStop:");
        synchronized (mSync) {
            if (mUSBMonitor != null) {
                mUSBMonitor.unregister();
            }
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) Log.v(TAG, "onDestroy:");
        synchronized (mSync) {
            isActive = isPreview = false;
            if (mUVCCamera != null) {
                mUVCCamera.destroy();
                mUVCCamera = null;
            }
            if (mUSBMonitor != null) {
                mUSBMonitor.destroy();
                mUSBMonitor = null;
            }
        }
        mUVCCameraView = null;
        mCameraButton = null;
        super.onDestroy();
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if (mUVCCamera == null) {
                // XXX calling CameraDialog.showDialog is necessary at only first time(only when app has no permission).
                CameraDialog.showDialog(FullscreenActivity.this);
            } else {
                synchronized (mSync) {
                    mUVCCamera.destroy();
                    mUVCCamera = null;
                    isActive = isPreview = false;
                }
            }
        }
    };

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onAttach:");
            Toast.makeText(FullscreenActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            if (DEBUG) Log.v(TAG, "onConnect:");
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.destroy();
                }
                isActive = isPreview = false;
            }
            baseActivity.queueEvent(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        final UVCCamera camera = new UVCCamera();
                        camera.open(ctrlBlock);
                        if (DEBUG) Log.i(TAG, "supportedSize:" + camera.getSupportedSize());

                        List<Size> mjpeg_camera_sizes = UVCCamera.getSupportedSize(6, camera.getSupportedSize());
                        // Pick the size that is closest to our required resolution
                        int required_width = GetScreenWidth();
                        int required_height = GetScreenHeight();
                        int required_area = required_width * required_height;
                        int preview_width = 0;
                        int preview_height = 0;
                        int error = Integer.MAX_VALUE; // trying to get this as small as possible
                        for (Size s : mjpeg_camera_sizes) {
                            // calculate the area for each camera size
                            int s_area = s.width * s.height;
                            // calculate the difference between this size and the target size
                            int abs_error = Math.abs(s_area - required_area);
                            // check if the abs_error is smaller than what we have already
                            // then use the new size
                            if (abs_error < error){
                                preview_width = s.width;
                                preview_height = s.height;
                                error = abs_error;
                            }
                        }

                        try {
                            camera.setPreviewSize(preview_width, preview_height, UVCCamera.FRAME_FORMAT_MJPEG);
                        } catch (final IllegalArgumentException e) {
                            try {
                                // fallback to YUV mode

                                List<Size> yuv_camera_sizes = UVCCamera.getSupportedSize(4, camera.getSupportedSize());

                                // find closest matching size
                                // Pick the size that is closest to our required resolution
                                int yuv_preview_width = 0;
                                int yuv_preview_height = 0;
                                int yuv_error = Integer.MAX_VALUE; // trying to get this as small as possible
                                for (Size s : yuv_camera_sizes) {
                                    // calculate the area for each camera size
                                    int s_area = s.width * s.height;
                                    // calculate the difference between this size and the target size
                                    int abs_error = Math.abs(s_area - required_area);
                                    // check if the abs_error is smaller than what we have already
                                    // then use the new size
                                    if (abs_error < yuv_error) {
                                        yuv_preview_width = s.width;
                                        yuv_preview_height = s.height;
                                        yuv_error = abs_error;
                                    }
                                }

                                camera.setPreviewSize(yuv_preview_width, yuv_preview_height, UVCCamera.FRAME_FORMAT_YUYV);
                            } catch (final IllegalArgumentException e1) {
                                camera.destroy();
                                return;
                            }
                        }
                        mPreviewSurface = mUVCCameraView.getHolder().getSurface();
                        if (mPreviewSurface != null) {
                            isActive = true;
                            camera.setPreviewDisplay(mPreviewSurface);
                            camera.startPreview();
                            isPreview = true;
                        }
                        synchronized (mSync) {
                            mUVCCamera = camera;
                        }
                    }
                }
            }, 0);
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.v(TAG, "onDisconnect:");
            // XXX you should check whether the comming device equal to camera device that currently using
            baseActivity.queueEvent(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        if (mUVCCamera != null) {
                            mUVCCamera.close();
                            if (mPreviewSurface != null) {
                                mPreviewSurface.release();
                                mPreviewSurface = null;
                            }
                            isActive = isPreview = false;
                        }
                    }
                }
            }, 0);
        }

        @Override
        public void onDettach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onDettach:");
            Toast.makeText(FullscreenActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
        }
    };

    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            baseActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // FIXME
                }
            }, 0);
        }
    }

    private final SurfaceHolder.Callback mSurfaceViewCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(final SurfaceHolder holder) {
            if (DEBUG) Log.v(TAG, "surfaceCreated:");
        }

        @Override
        public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
            if ((width == 0) || (height == 0)) return;
            if (DEBUG) Log.v(TAG, "surfaceChanged:");
            mPreviewSurface = holder.getSurface();
            synchronized (mSync) {
                if (isActive && !isPreview && (mUVCCamera != null)) {
                    mUVCCamera.setPreviewDisplay(mPreviewSurface);
                    mUVCCamera.startPreview();
                    isPreview = true;
                }
            }
        }

        @Override
        public void surfaceDestroyed(final SurfaceHolder holder) {
            if (DEBUG) Log.v(TAG, "surfaceDestroyed:");
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.stopPreview();
                }
                isPreview = false;
            }
            mPreviewSurface = null;
        }
    };

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
