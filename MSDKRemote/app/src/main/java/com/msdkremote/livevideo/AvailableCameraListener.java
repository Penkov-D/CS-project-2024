package com.msdkremote.livevideo;

import androidx.annotation.NonNull;

import java.io.OutputStream;
import java.util.List;

import dji.sdk.keyvalue.value.common.ComponentIndexType;
import dji.v5.manager.datacenter.camera.CameraStreamManager;
import dji.v5.manager.interfaces.ICameraStreamManager;

public class AvailableCameraListener
    implements ICameraStreamManager.AvailableCameraUpdatedListener
{
    private boolean listenerOn = false;
    private final ICameraStreamManager streamManager = CameraStreamManager.getInstance();

    private ComponentIndexType cameraOn = null;
    private final CameraListener cameraListener = new CameraListener();

    public AvailableCameraListener() {}

    @Override
    public synchronized void onAvailableCameraUpdated(@NonNull List<ComponentIndexType> availableCameraList)
    {
        if (!listenerOn)
            return;

        if (cameraOn != null && !availableCameraList.contains(cameraOn)) {
            streamManager.removeReceiveStreamListener(cameraListener);
            cameraOn = null;
        }

        if (cameraOn == null && !availableCameraList.isEmpty()) {
            cameraOn = availableCameraList.get(0);
            streamManager.addReceiveStreamListener(cameraOn, cameraListener);
        }
    }

    public synchronized void startListener(OutputStream outStream)
    {
        if (listenerOn)
            return;

        listenerOn = true;
        cameraListener.setOutputStream(outStream);
        streamManager.addAvailableCameraUpdatedListener(this);
    }

    public synchronized void stopListener()
    {
        if (!listenerOn)
            return;

        listenerOn = false;
        streamManager.removeAvailableCameraUpdatedListener(this);

        if (cameraOn != null) {
            streamManager.removeReceiveStreamListener(cameraListener);
            cameraOn = null;
        }
    }
}
