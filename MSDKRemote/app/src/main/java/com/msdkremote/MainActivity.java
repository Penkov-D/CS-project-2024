package com.msdkremote;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.msdkremote.livecontrol.ControlServer;
import com.msdkremote.livevideo.VideoServerManager;

import java.util.List;

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
    }

    @Override
    protected synchronized void onResume() {
        super.onResume();

        // Calling onRegistered after MSDK registered and only once per launch.
        if (!wasRegistered && SDKManager.getInstance().isRegistered()) {
            wasRegistered = true;
            onRegistered();
        }
    }

    @Override
    protected synchronized void onDestroy() {
        super.onDestroy();

        // Calling onUnregistered if app closes and onRegistered was called.
        if (wasRegistered) {
            onUnregistered();
        }
    }

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

        ControlServer controlServer = new ControlServer(this, 9998);

        // Start video broadcast on phone
        setVideoSurface();
    }

    private void setVideoSurface()
    {
        ICameraStreamManager cameraStreamManager = CameraStreamManager.getInstance();

        cameraStreamManager.addAvailableCameraUpdatedListener(
            availableCameraList -> {
                if (availableCameraList.isEmpty())
                    return;

                SurfaceView surfaceView = findViewById(R.id.mainVideo);
                Surface surface = surfaceView.getHolder().getSurface();

                cameraStreamManager.putCameraStreamSurface(
                        availableCameraList.get(0),
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
        }
        catch (InterruptedException e) {
            Log.e(TAG, "onUnregistered: Interrupted Exception occurred on UI thread");
        }
    }

    // Start Server button action
    public void StartVideoServer (View view)
    {
        VideoServerManager.getInstance().startServer(9999);

        IVirtualStickManager stickManager = VirtualStickManager.getInstance();

        stickManager.enableVirtualStick(
                new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "onSuccess", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Toast.makeText(MainActivity.this, "onFailure - " + idjiError.errorCode(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // Stop Server button action
    public void StopVideoServer (View view)
    {
        try {
            VideoServerManager.getInstance().killServer();
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted Exception occurred on UI thread");
        }
    }

    public void StartSurfaceView(View view)
    {
        setVideoSurface();
    }
}
