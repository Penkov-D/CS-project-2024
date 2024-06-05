package com.msdkremote;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.msdkremote.livevideo.VideoServerManager;

import dji.v5.manager.SDKManager;

public class MainActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    private boolean wasRegistered = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!SDKManager.getInstance().isRegistered())
        {
            Log.i(TAG, "App is not registered yet!");
            // Start the loading screen
            this.startActivity(
                    new Intent(this, LoadingActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            );
        }
        else
        {
            Log.i(TAG, "App is registered!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!wasRegistered && SDKManager.getInstance().isRegistered()) {
            wasRegistered = true;
            onRegistered();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (wasRegistered) {
            onUnregistered();
        }
    }

    /**
     * This method will be called when the MSDK is activated.
     * Put here all the MSDK functions that run on start.
     * Note, this is on the UI thread, so no blocking functions.
     */
    private void onRegistered()
    {
        VideoServerManager.getInstance().startServer(9999);
    }

    /**
     * This method will be called when the app is closed.
     * Put here all the "cleanup" methods from the MSDK.
     * Note, this method will be called only if onRegistered method was called.
     */
    private void onUnregistered()
    {
        try {
            VideoServerManager.getInstance().killServer();
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted Exception occurred on UI thread");
        }
    }

    public void StartVideoServer (View view)
    {
        VideoServerManager.getInstance().startServer(9999);
    }

    public void StopVideoServer (View view)
    {
        try {
            VideoServerManager.getInstance().killServer();
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted Exception occurred on UI thread");
        }
    }
}
