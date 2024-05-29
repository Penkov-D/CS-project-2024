package com.msdkremote;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import dji.v5.manager.SDKManager;

public class MainActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

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
}
