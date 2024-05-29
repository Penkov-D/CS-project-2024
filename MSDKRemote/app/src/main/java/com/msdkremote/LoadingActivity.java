package com.msdkremote;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import dji.v5.common.error.IDJIError;
import dji.v5.common.register.DJISDKInitEvent;
import dji.v5.manager.SDKManager;
import dji.v5.manager.interfaces.SDKManagerCallback;

public class LoadingActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    private void printLoadingMessage (String message) {
        runOnUiThread(() -> ((TextView) findViewById(R.id.logTextView)).setText(message));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loading_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize MSDK. It is recommended to put the initialization logic in Application.
        // Of course, you can put it anywhere according to your needs.
        printLoadingMessage("Initialize MSDK");

        SDKManager.getInstance().init(
                getApplicationContext(),
                new SDKManagerCallback() {
                    @Override
                    public void onInitProcess(DJISDKInitEvent event, int totalProcess) {
                        printLoadingMessage("Init process");
                        Log.i(TAG, "onInitProcess: ");
                        if (event == DJISDKInitEvent.INITIALIZE_COMPLETE) {
                            printLoadingMessage("Init done, register app");
                            SDKManager.getInstance().registerApp();
                        }
                    }
                    @Override
                    public void onRegisterSuccess() {
                        Log.i(TAG, "onRegisterSuccess: ");
                        printLoadingMessage("Register success");
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> finish(), 500);
                    }
                    @Override
                    public void onRegisterFailure(IDJIError error) {
                        Log.i(TAG, "onRegisterFailure: ");
                        printLoadingMessage("Register success");
                    }
                    @Override
                    public void onProductConnect(int productId) {
                        Log.i(TAG, "onProductConnect: ");
                    }
                    @Override
                    public void onProductDisconnect(int productId) {
                        Log.i(TAG, "onProductDisconnect: ");
                    }
                    @Override
                    public void onProductChanged(int productId) {
                        Log.i(TAG, "onProductChanged: ");
                    }
                    @Override
                    public void onDatabaseDownloadProgress(long current, long total) {
                        Log.i(TAG, String.format("onDatabaseDownloadProgress: %d / %d", current, total));
                        printLoadingMessage("Database download");
                    }
                }
        );
    }
}