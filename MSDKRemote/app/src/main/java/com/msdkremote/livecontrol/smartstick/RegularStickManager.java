package com.msdkremote.livecontrol.smartstick;

import android.util.Log;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dji.sdk.keyvalue.key.DJIActionKeyInfo;
import dji.sdk.keyvalue.key.DJIKey;
import dji.sdk.keyvalue.key.DJIKeyInfo;
import dji.sdk.keyvalue.key.FlightControllerKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.sdk.keyvalue.value.common.EmptyMsg;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.manager.KeyManager;
import dji.v5.manager.aircraft.virtualstick.IStick;
import dji.v5.manager.aircraft.virtualstick.VirtualStickManager;
import dji.v5.manager.interfaces.IKeyManager;
import kotlin.NotImplementedError;
import kotlin.Result;

public class RegularStickManager implements StickManager
{
    private final String TAG = this.getClass().getSimpleName();

    private boolean isVirtualStickActive = false;

    public RegularStickManager() {

    }

    public synchronized void startStickManagement() {
        isVirtualStickActive = true;
    }

    public synchronized void stopStickManagement() {
        isVirtualStickActive = false;
    }

    private double roundToZero(double value)
    {
        if (!Double.isFinite(value))
            return value;

        if (value < 0.0)
            value = Math.ceil(value);

        if (value > 0.0)
            value = Math.floor(value);

        return value;
    }

    @IntRange(from = -660, to = 660)
    private synchronized int floatToSickValue(
            @FloatRange(from = -1.0, to = 1.0) double value)
    {
        // Make sure value is not infinity or NaN
        if (!Double.isFinite(value))
            value = 0.0;

        // Convert from [-1.0, 1.0] to [-660.0, 660.0]
        int intValue = (int) roundToZero(value * 660.0);

        if (value >  660.0) intValue =  660;
        if (value < -660.0) intValue = -660;

        return intValue;
    }

    public synchronized void setLeftStick(
            @FloatRange(from = -1.0, to = 1.0) double hValue,
            @FloatRange(from = -1.0, to = 1.0) double vValue)
    {
        if (!isVirtualStickActive) return;
        if (!Double.isFinite(hValue) || hValue < -1.0 || hValue > 1.0) hValue = 0.0;
        if (!Double.isFinite(vValue) || vValue < -1.0 || vValue > 1.0) vValue = 0.0;

        IStick lStick = VirtualStickManager.getInstance().getLeftStick();

        int hStick = floatToSickValue(hValue);
        int vStick = floatToSickValue(vValue);

        Log.i(this.TAG, "Right stick : " + hStick + ", " + vStick);

        lStick.setHorizontalPosition(hStick);
        lStick.setVerticalPosition(vStick);
    }

    public synchronized void setRightStick(
            @FloatRange(from = -1.0, to = 1.0) double hValue,
            @FloatRange(from = -1.0, to = 1.0) double vValue)
    {
        if (!isVirtualStickActive) return;
        if (!Double.isFinite(hValue) || hValue < -1.0 || hValue > 1.0) hValue = 0.0;
        if (!Double.isFinite(vValue) || vValue < -1.0 || vValue > 1.0) vValue = 0.0;

        IStick rStick = VirtualStickManager.getInstance().getRightStick();

        int hStick = floatToSickValue(hValue);
        int vStick = floatToSickValue(vValue);

        Log.i(this.TAG, "Right stick : " + hStick + ", " + vStick);

        rStick.setHorizontalPosition(hStick);
        rStick.setVerticalPosition(vStick);
    }

    public synchronized void setSticks(
            @FloatRange(from = -1.0, to = 1.0) double leftHValue,
            @FloatRange(from = -1.0, to = 1.0) double leftVValue,
            @FloatRange(from = -1.0, to = 1.0) double rightHValue,
            @FloatRange(from = -1.0, to = 1.0) double rightVValue)
    {
        setLeftStick(leftHValue, leftVValue);
        setRightStick(rightHValue, rightVValue);
    }


    private<T> void runAction(
            DJIActionKeyInfo<T, EmptyMsg> actionKey, @Nullable ActionCallback callback)
    {
        IKeyManager keyManager = KeyManager.getInstance();

        keyManager.performAction(
                KeyTools.createKey(actionKey),
                new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>()
                {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        if (callback != null) {
                            callback.onFailure(idjiError);
                        }
                    }
                }
        );
    }

    @Override
    public void takeoff(@Nullable ActionCallback callback)
    {
        Log.i(this.TAG, "takeoff");
        runAction(FlightControllerKey.KeyStartTakeoff, callback);
    }

    @Override
    public void land(@Nullable ActionCallback callback)
    {
        Log.i(this.TAG, "land");
        runAction(FlightControllerKey.KeyStartAutoLanding, callback);
        runAction(FlightControllerKey.KeyConfirmLanding, callback);
    }
}
