package com.msdkremote;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.msdkremote.commandserver.CommandServer;
import com.msdkremote.livecontrol.ControlServerManager;
import com.msdkremote.livecontrol.regularStickManager.RegularStickManager;
import com.msdkremote.livecontrol.StickManager;
import com.msdkremote.livevideo.VideoServerManager;
import com.msdkremote.networkstate.NetworkMonitor;

import dji.sdk.keyvalue.value.common.ComponentIndexType;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.manager.SDKManager;
import dji.v5.manager.aircraft.virtualstick.VirtualStickManager;
import dji.v5.manager.datacenter.camera.CameraStreamManager;
import dji.v5.manager.interfaces.ICameraStreamManager;
import dji.v5.manager.interfaces.IVirtualStickManager;

public class MainActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    // Hold whether the onRegister method was called
    private boolean wasRegistered = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
    }


    private void setVideoSurface()
    {
        ICameraStreamManager cameraStreamManager = CameraStreamManager.getInstance();

        SurfaceView surfaceView = findViewById(R.id.mainVideo);
        Surface surface = surfaceView.getHolder().getSurface();

        cameraStreamManager.putCameraStreamSurface(
                ComponentIndexType.LEFT_OR_MAIN,
                surface,
                surfaceView.getWidth(),
                surfaceView.getHeight(),
                ICameraStreamManager.ScaleType.CENTER_INSIDE
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

    public void StartKeyActivity(View view) {
        startActivity(new Intent(this, KeyActivity.class));
    }

    @Override
    public void onBackPressed() {
        // Do noting when back pressed (e.g. don't close activity)
        // super.onBackPressed();
    }


}
