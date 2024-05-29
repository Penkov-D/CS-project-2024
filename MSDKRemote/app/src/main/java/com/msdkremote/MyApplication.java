package com.msdkremote;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import dji.v5.common.error.IDJIError;
import dji.v5.common.register.DJISDKInitEvent;
import dji.v5.manager.SDKManager;
import dji.v5.manager.interfaces.SDKManagerCallback;

public class MyApplication extends Application
{
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // Before calling install, do not call any MSDK related
        // Basically, chines viruses
        com.secneo.sdk.Helper.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize MSDK. It is recommended to put the initialization logic in Application.
        // Of course, you can put it anywhere according to your needs.
        SDKManager.getInstance().init(
            this,
            new SDKManagerCallback() {
                @Override
                public void onInitProcess(DJISDKInitEvent event, int totalProcess) {
                    Log.i(TAG, "onRegisterSuccess:");
                    if (event == DJISDKInitEvent.INITIALIZE_COMPLETE) {
                        SDKManager.getInstance().registerApp();
                    }
                }
                @Override
                public void onRegisterSuccess() {
                    Log.i(TAG, "onRegisterSuccess: ");
                }
                @Override
                public void onRegisterFailure(IDJIError error) {
                    Log.i(TAG, "onRegisterFailure: ");
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
                }
            }
        );
    }
}
