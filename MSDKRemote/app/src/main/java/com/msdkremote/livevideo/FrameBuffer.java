package com.msdkremote.livevideo;

import java.util.LinkedList;
import java.util.Queue;

class FrameBuffer
{
    private final Queue<Frame> frames = new LinkedList<>();
    private final int maxBufferSize;
    private int bufferSize = 0;

    public FrameBuffer(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }

    public int getMaxBufferSize() {
        return this.maxBufferSize;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public synchronized void addFrame(Frame frame)
    {
        this.frames.add(frame);
        this.bufferSize += frame.getSize();

        while (this.bufferSize > this.maxBufferSize)
        {
            Frame removedFrame = this.frames.poll();
            if (removedFrame == null)
                this.bufferSize = 0;
            else
                this.bufferSize -= removedFrame.getSize();
        }

    }
}
