package com.msdkremote.livecontrol.smartstick;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dji.v5.manager.aircraft.virtualstick.IStick;
import dji.v5.manager.aircraft.virtualstick.VirtualStickManager;

public interface StickManager
{
    public void startStickManagement();

    public void stopStickManagement();

    public void setSticks(
            @FloatRange(from = -1.0, to = 1.0) double leftHValue,
            @FloatRange(from = -1.0, to = 1.0) double leftVValue,
            @FloatRange(from = -1.0, to = 1.0) double rightHValue,
            @FloatRange(from = -1.0, to = 1.0) double rightVValue);

    public void takeoff(@Nullable ActionCallback callback);

    public void land(@Nullable ActionCallback callback);
}
