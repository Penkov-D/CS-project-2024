package com.msdkremote.livevideo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dji.v5.manager.interfaces.ICameraStreamManager;

enum FrameCodec
{
    CODEC_H264("H264"),
    CODEC_H265("H265"),
    CODEC_UNKNOWN("UNKNOWN");

    private final String description;

    private FrameCodec(@NonNull String description) {
        this.description = description;
    }

    @Nullable
    public static FrameCodec getFrameCodec(ICameraStreamManager.MimeType mimeType)
    {
        if (mimeType == null)
            return null;

        switch (mimeType)
        {
            case H264:
                return CODEC_H264;
            case H265:
                return CODEC_H265;
            default:
                return CODEC_UNKNOWN;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "CODEC : " + description;
    }
}
