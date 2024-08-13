package com.msdkremote;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import dji.sdk.keyvalue.key.BatteryKey;
import dji.sdk.keyvalue.key.FlightControllerKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.sdk.keyvalue.value.common.LocationCoordinate3D;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.manager.KeyManager;
import dji.v5.manager.interfaces.IKeyManager;

public class KeyActivity extends AppCompatActivity {
    private final String TAG = KeyActivity.class.getSimpleName();
    private final IKeyManager keyManager = KeyManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keys);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        updateBattery();
        updateLocation();

        keyManager.listen(
                KeyTools.createKey(BatteryKey.KeyChargeRemainingInPercent),
                this,
                (oldVal, newVal) -> {
                    TextView textView = (TextView) findViewById(R.id.BatteryPercentValue);
                    textView.setText(String.format("%s%%", newVal));
                    Log.d(TAG, "Battery Level Changed: " + oldVal + "% -> " + newVal + "%");
                }
        );

        keyManager.listen(
                KeyTools.createKey(FlightControllerKey.KeyAircraftLocation3D),
                this,
                (oldLocation, location) -> {
                    if (location != null) {
                        ((TextView) findViewById(R.id.LongitudeValue)).setText(String.format("%s", location.getLongitude()));
                        ((TextView) findViewById(R.id.LatitudeValue)).setText(String.format("%s", location.getLatitude()));
                        ((TextView) findViewById(R.id.AltitudeValue)).setText(String.format("%s", location.getAltitude()));
                        Log.d(TAG, "Location(lon:lat:alt): " + location.getLongitude() + ":" + location.getLatitude() + ":" + location.getAltitude());
                    }
                }
        );
    }

    private void updateBattery(){
        keyManager.getValue(
            KeyTools.createKey(BatteryKey.KeyChargeRemainingInPercent),
            new CommonCallbacks.CompletionCallbackWithParam<Integer>() {
                @Override
                public void onSuccess(Integer value) {
                    TextView textView = (TextView) findViewById(R.id.BatteryPercentValue);
                    textView.setText(String.format("%s%%", value));
                    Log.d(TAG, "Battery Level: " + value + "%");
                }
                @Override
                public void onFailure(@NonNull IDJIError idjiError) {
                    Log.e(TAG, "Error getting battery level: " + idjiError.description());

                }
            });
    }

    private void updateLocation(){
        keyManager.getValue(
                KeyTools.createKey(FlightControllerKey.KeyAircraftLocation3D),
                new CommonCallbacks.CompletionCallbackWithParam<LocationCoordinate3D>() {
                    @Override
                    public void onSuccess(LocationCoordinate3D location) {
                        ((TextView) findViewById(R.id.LongitudeValue)).setText(String.format("%s", location.getLongitude()));
                        ((TextView) findViewById(R.id.LatitudeValue)).setText(String.format("%s", location.getLatitude()));
                        ((TextView) findViewById(R.id.AltitudeValue)).setText(String.format("%s", location.getAltitude()));
                        Log.d(TAG, "Location(lon:lat:alt): " + location.getLongitude() + ":" + location.getLatitude() + ":" + location.getAltitude());
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.e(TAG, "Error getting location3d: " + idjiError.description());

                    }
                });
    }

    protected synchronized void onDestroy() {
        super.onDestroy();
        keyManager.cancelListen(this);
    }
}
