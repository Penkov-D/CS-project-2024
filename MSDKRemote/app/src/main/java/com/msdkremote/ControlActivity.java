package com.msdkremote;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import dji.sdk.keyvalue.key.FlightAssistantKey;
import dji.sdk.keyvalue.key.FlightControllerKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.sdk.keyvalue.value.common.EmptyMsg;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.manager.KeyManager;
import dji.v5.manager.aircraft.virtualstick.VirtualStickManager;
import dji.v5.manager.interfaces.IKeyManager;
import dji.v5.manager.interfaces.IVirtualStickManager;

public class ControlActivity extends AppCompatActivity
{
    private final String TAG = ControlActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        IVirtualStickManager stickManager = VirtualStickManager.getInstance();

        stickManager.enableVirtualStick(
                new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(
                                ControlActivity.this,
                                "Virtual Stick Success",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Toast.makeText(
                                ControlActivity.this,
                                "Virtual Stick Failed\n" + idjiError.errorCode(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        findViewById(R.id.MoveForward).setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent)
                    {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            Log.i(TAG, "ACTION DOWN");
                            stickManager.getRightStick().setVerticalPosition(30);
                        }
                        else if (motionEvent.getAction() == MotionEvent.ACTION_UP
                                || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                            Log.i(TAG, "ACTION UP");
                            stickManager.getRightStick().setVerticalPosition(0);
                        }
                        return false;
                    }
                }
        );

        findViewById(R.id.MoveBack).setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent)
                    {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            Log.i(TAG, "ACTION DOWN");
                            stickManager.getRightStick().setVerticalPosition(-30);
                        }
                        else if (motionEvent.getAction() == MotionEvent.ACTION_UP
                                || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                            Log.i(TAG, "ACTION UP");
                            stickManager.getRightStick().setVerticalPosition(0);
                        }
                        return false;
                    }
                }
        );

        findViewById(R.id.MoveLeft).setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent)
                    {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            Log.i(TAG, "ACTION DOWN");
                            stickManager.getRightStick().setHorizontalPosition(-30);
                        }
                        else if (motionEvent.getAction() == MotionEvent.ACTION_UP
                                || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                            Log.i(TAG, "ACTION UP");
                            stickManager.getRightStick().setHorizontalPosition(0);
                        }
                        return false;
                    }
                }
        );

        findViewById(R.id.MoveRight).setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent)
                    {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            Log.i(TAG, "ACTION DOWN");
                            stickManager.getRightStick().setHorizontalPosition(30);
                        }
                        else if (motionEvent.getAction() == MotionEvent.ACTION_UP
                                || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                            Log.i(TAG, "ACTION UP");
                            stickManager.getRightStick().setHorizontalPosition(0);
                        }
                        return false;
                    }
                }
        );
    }



    public void buttonTakeoff(View view)
    {
        KeyManager.getInstance().performAction(
                KeyTools.createKey(FlightControllerKey.KeyStartTakeoff),
                new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {}

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Toast.makeText(ControlActivity.this, idjiError.errorCode(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public void buttonLand(View view)
    {
        KeyManager.getInstance().performAction(
                KeyTools.createKey(FlightControllerKey.KeyStartAutoLanding),
                new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {}

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Toast.makeText(ControlActivity.this, idjiError.errorCode(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        KeyManager.getInstance().performAction(
                KeyTools.createKey(FlightControllerKey.KeyConfirmLanding),
                new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {}

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Toast.makeText(ControlActivity.this, idjiError.errorCode(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}