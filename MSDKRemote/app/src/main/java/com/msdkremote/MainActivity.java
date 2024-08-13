package com.msdkremote;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.msdkremote.commandserver.CommandServer;
import com.msdkremote.commandserver.CommandServerStateListener;
import com.msdkremote.livecontrol.ActionCallback;
import com.msdkremote.livecontrol.ControlServerManager;
import com.msdkremote.livecontrol.regularStickManager.RegularStickManager;
import com.msdkremote.livevideo.VideoServerManager;
import com.msdkremote.networkstate.NetworkMonitor;

import java.net.InetAddress;
import java.util.Locale;

import dji.sdk.keyvalue.key.FlightControllerKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.sdk.keyvalue.key.RemoteControllerKey;
import dji.sdk.keyvalue.value.common.ComponentIndexType;
import dji.sdk.keyvalue.value.remotecontroller.BatteryInfo;
import dji.sdk.keyvalue.value.remotecontroller.RemoteControllerType;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.manager.KeyManager;
import dji.v5.manager.SDKManager;
import dji.v5.manager.datacenter.camera.CameraStreamManager;
import dji.v5.manager.interfaces.ICameraStreamManager;

public class MainActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    // Hold whether the onRegister method was called
    private boolean wasRegistered = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        // Launch the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register the MSDK once it was opened
        if (!SDKManager.getInstance().isRegistered()) {
            Log.i(TAG, "onCreate(): Registering the MSDK.");
            Log.i(TAG, "onCreate(): Moving to loading screen.");

            // Start the loading screen
            this.startActivity(
                    new Intent(this, LoadingActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            );
        }
        else {
            Log.i(TAG, "OnCreate(): App is registered!");
        }

        // Register auto IP monitor
        NetworkMonitor monitor = new NetworkMonitor(this);
        monitor.registerListener((networkType, address) -> {
            MainActivity.this.runOnUiThread(() -> {
                TextView textView = (TextView) findViewById(R.id.networkStateTextView);
                textView.setText(String.format(
                        "Network type : %s, address : %s",
                        networkType,
                        address.getHostAddress()
                ));
            });
        });

        CheckBox checkBoxVideo = findViewById(R.id.checkBoxVideoServer);
        CheckBox checkBoxControl = findViewById(R.id.checkBoxControlServer);

        TextView textViewVideoStatus = findViewById(R.id.textViewVideoStatus);
        TextView textViewControlStatus = findViewById(R.id.textViewControlStatus);

        // Set video server check box handler
        checkBoxVideo.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {
                        VideoServerManager.getInstance().startServer(9999);
                        textViewVideoStatus.setText(R.string.video_status_running);
                    }
                    else {
                        try {
                            VideoServerManager.getInstance().killServer();
                        } catch (InterruptedException e) {
                            Log.w(TAG, "Interrupted exception while closing video server", e);
                        }
                        textViewVideoStatus.setText(R.string.video_server_not_running);
                    }
                }
        );

        // Set control server status text view
        ControlServerManager.getInstance().setStateListener(
                new CommandServerStateListener() {
                    @Override
                    public void onServerRunning() {
                        textViewControlStatus.setText(R.string.control_server_running);
                    }

                    @Override
                    public void onServerClosed() {
                        textViewControlStatus.setText(R.string.control_server_not_running);
                    }

                    @Override
                    public void onServerException(Exception e) {
                        textViewControlStatus.setText(R.string.control_server_not_running_exception);
                        Log.w(TAG, "Control server suffered from exception", e);
                    }

                    @Override
                    public void onClientConnected(InetAddress address) {
                        textViewControlStatus.setText(String.format("%s - %s",
                                getString(R.string.control_server_client_connected),
                                address.getHostAddress()));
                    }

                    @Override
                    public void onClientDisconnected() {
                        textViewControlStatus.setText(R.string.control_server_running);
                    }
                }
        );

        // Set control server check box handler
        checkBoxControl.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {
                        ControlServerManager.getInstance().startServer(9998);
                    }
                    else {
                        try {
                            ControlServerManager.getInstance().killServer();
                        } catch (InterruptedException e) {
                            Log.w(TAG, "Interrupted exception while closing control server", e);
                        }
                    }
                }
        );
    }


    @Override
    protected synchronized void onResume() {
        super.onResume();

        // Calling onRegistered after MSDK registered and only once per launch.
        if (!wasRegistered && SDKManager.getInstance().isRegistered()) {
            wasRegistered = true;
            onRegistered();
        }

        // Start video broadcast on phone
        if (SDKManager.getInstance().isRegistered())
            setVideoSurface();
    }


    @Override
    protected synchronized void onDestroy() {
        super.onDestroy();

        // Calling onUnregistered if app closes and onRegistered was called.
        if (wasRegistered) {
            onUnregistered();
        }
    }

    private CommandServer serverThread;


    /**
     * This method will be called when the MSDK is activated.
     * Put here all the MSDK functions that run on start.
     * Note, this from the UI thread, so no blocking functions.
     */
    private void onRegistered()
    {
        Log.i(TAG, "onRegistered(): starting default services.");

        // Start video server
        VideoServerManager.getInstance().startServer(9999);

        // Start command server
        ControlServerManager.getInstance().startServer(9998);

        // Set controller status views
        setControllerViews();

        // Set drone status views
        setDroneViews();
    }


    private void setControllerViews()
    {
        ImageView icon = findViewById(R.id.iconViewController);
        TextView type = findViewById(R.id.textViewRemoteType);
        TextView battery = findViewById(R.id.textViewRemoteBattery);

        // Listen for connection
        KeyManager.getInstance().listen(
                KeyTools.createKey(RemoteControllerKey.KeyConnection),
                this,
                (ignore, isConnected) -> {
                    if ((isConnected != null && isConnected)) {
                        icon.setImageResource(R.drawable.controller_green);
                    }
                    else {
                        icon.setImageResource(R.drawable.controller_grey);
                    }
                }
        );

        // Monitor battery level
        KeyManager.getInstance().listen(
                KeyTools.createKey(RemoteControllerKey.KeyBatteryInfo),
                this,
                (ignore, batteryInfo) -> {
                    if (batteryInfo == null) {
                        battery.setText("---%");
                    }
                    else {
                        battery.setText(String.format(
                                Locale.ENGLISH,
                                "%d%%",
                                batteryInfo.getBatteryPercent()));
                    }
                }
        );

        // Monitor Controller type
        KeyManager.getInstance().listen(
                KeyTools.createKey(RemoteControllerKey.KeyFirmwareVersion),
                this,
                (ignore, firmwareVersion) -> {
                    type.setText(firmwareVersion == null ? "Disconnected" : firmwareVersion);
                }
        );

    }

    private void setDroneViews()
    {
        ImageView icon = findViewById(R.id.iconViewDrone);
        TextView type = findViewById(R.id.textViewDroneType);
        TextView battery = findViewById(R.id.textViewDroneBattery);


        // Listen for connection
        KeyManager.getInstance().listen(
                KeyTools.createKey(FlightControllerKey.KeyConnection),
                this,
                (ignore, isConnected) -> {
                    if ((isConnected != null && isConnected)) {
                        icon.setImageResource(R.drawable.drone_green);
                    }
                    else {
                        icon.setImageResource(R.drawable.drone_grey);
                    }
                }
        );

        // Monitor battery level
        KeyManager.getInstance().listen(
                KeyTools.createKey(FlightControllerKey.KeyBatteryPowerPercent),
                this,
                (ignore, batteryPresent) -> {
                    if (batteryPresent == null) {
                        battery.setText("---%");
                    }
                    else {
                        battery.setText(String.format(
                                Locale.ENGLISH,
                                "%d%%",
                                batteryPresent));
                    }
                }
        );

        // Monitor Controller type
        KeyManager.getInstance().listen(
                KeyTools.createKey(FlightControllerKey.KeyAircraftName),
                this,
                (ignore, aircraftName) -> {
                    type.setText(aircraftName == null ? "Disconnected" : aircraftName);
                }
        );
    }


    private void setVideoSurface()
    {
        ICameraStreamManager cameraStreamManager = CameraStreamManager.getInstance();

        cameraStreamManager.addAvailableCameraUpdatedListener(
                availableCameraList -> {

                    if (!availableCameraList.contains(ComponentIndexType.LEFT_OR_MAIN))
                        return;

                    SurfaceView surfaceView = findViewById(R.id.mainVideo);
                    Surface surface = surfaceView.getHolder().getSurface();

                    cameraStreamManager.removeCameraStreamSurface(surface);
                    cameraStreamManager.putCameraStreamSurface(
                            ComponentIndexType.LEFT_OR_MAIN,
                            surface,
                            surfaceView.getWidth(),
                            surfaceView.getHeight(),
                            ICameraStreamManager.ScaleType.CENTER_INSIDE
                    );
                }
        );
    }


    /**
     * This method will be called when the app is closed.
     * Put here all the "cleanup" methods from the MSDK.
     * Note, this method will be called only if onRegistered method was called.
     */
    private void onUnregistered()
    {
        try {
            // Close video server broadcast
            VideoServerManager.getInstance().killServer();

            // Close control server
            ControlServerManager.getInstance().killServer();
        }
        catch (InterruptedException e) {
            Log.e(TAG, "onUnregistered: Interrupted Exception occurred on UI thread");
        }
    }


    public void StartControlActivity(View view) {
        startActivity(new Intent(this, ControlActivity.class));
    }


    @Override
    public void onBackPressed() {
        // Do noting when back pressed (e.g. don't close activity)
        // super.onBackPressed();
    }


    public void emergencyStop(View view)
    {
        // Kill all control activity
        try {
            ControlServerManager.getInstance().killServer();

            ((CheckBox) findViewById(R.id.checkBoxControlServer))
                    .setChecked(false);
        }
        catch (InterruptedException e) {
            Log.w(TAG, "Received interrupt exception while emergency stop.", e);
        }

        RegularStickManager stickManager = new RegularStickManager();

        // Return control to the remote control
        stickManager.stopStickManagement(
                new ActionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Control restored", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Emergency stop - control restored.");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        Toast.makeText(MainActivity.this, "Couldn't get control", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error while emergency stop - retrieving control \n" + error.toString());
                    }
                }
        );

        // Land the aircraft
        stickManager.land(
                new ActionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Start landing", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Emergency stop - start landing.");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        Toast.makeText(MainActivity.this, "Couldn't land", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error while emergency stop - landing \n" + error.toString());
                    }
                }
        );
    }
}
