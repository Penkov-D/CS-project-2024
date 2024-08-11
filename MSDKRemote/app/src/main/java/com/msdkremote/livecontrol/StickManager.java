package com.msdkremote.livecontrol;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface StickManager
{
    public void startStickManagement(@NonNull ActionCallback callback);

    public void stopStickManagement(@NonNull ActionCallback callback);

    public void setSticks(
            @FloatRange(from = -1.0, to = 1.0) double leftHValue,
            @FloatRange(from = -1.0, to = 1.0) double leftVValue,
            @FloatRange(from = -1.0, to = 1.0) double rightHValue,
            @FloatRange(from = -1.0, to = 1.0) double rightVValue);

    public void takeoff(@Nullable ActionCallback callback);

    public void land(@Nullable ActionCallback callback);
}
