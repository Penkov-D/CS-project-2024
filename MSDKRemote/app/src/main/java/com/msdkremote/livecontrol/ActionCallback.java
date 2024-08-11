package com.msdkremote.livecontrol;

import androidx.annotation.NonNull;

import dji.v5.common.error.IDJIError;

public interface ActionCallback
{
    public void onSuccess();

    public void onFailure(@NonNull IDJIError error);
}
