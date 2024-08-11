package com.msdkremote.livevideo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dji.v5.manager.datacenter.camera.StreamInfo;
import dji.v5.manager.interfaces.ICameraStreamManager;

class Frame
{
    // The frame binary data
    private final byte[] fData;

    // Frame parameters
    private final int height, width;
    private final long presentationTimeMs;
    private final boolean isKeyFrame;

    // Stream parameters
    private final int frameRate;
    private final FrameCodec codec;

    public Frame (@NonNull  byte[] data, int offset, int length, @NonNull StreamInfo info)
    {
        // Copy the frame data
        fData = new byte[length];
        System.arraycopy(data, offset, fData, 0, length);

        // Set frame parameters
        this.height = info.getHeight();
        this.width  = info.getWidth();
        this.isKeyFrame = info.isKeyFrame();
        this.presentationTimeMs = info.getPresentationTimeMs();

        // Set stream parameters
        this.frameRate = info.getFrameRate();
        this.codec = FrameCodec.getFrameCodec(info.getMimeType());
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public long getPresentationTimeMs() {
        return this.presentationTimeMs;
    }

    public int getFrameRate() {
        return this.frameRate;
    }

    public boolean isKeyFrame() {
        return this.isKeyFrame;
    }

    public byte[] getData() {
        return this.fData;
    }

    public int getSize() {
        return fData.length;
    }

    public FrameCodec getCodec() {
        return this.codec;
    }
}
