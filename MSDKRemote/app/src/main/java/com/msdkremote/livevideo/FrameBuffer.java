package com.msdkremote.livevideo;

import java.util.LinkedList;
import java.util.Queue;

class FrameBuffer
{
    private final Queue<Frame> frames = new LinkedList<>();
    private final int maxBufferSize;
    private int bufferSize = 0;
    private boolean nextKeyFrame = true;

    public FrameBuffer(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }

    public int getMaxBufferSize() {
        return this.maxBufferSize;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public synchronized void nextKeyFrame() {
        this.nextKeyFrame = true;
    }

    public synchronized void addFrame(Frame frame)
    {
        this.frames.add(frame);
        this.bufferSize += frame.getSize();

        while (this.bufferSize > this.maxBufferSize)
        {
            Frame removedFrame = this.frames.poll();
            this.nextKeyFrame = true;

            if (removedFrame == null)
                this.bufferSize = 0;
            else
                this.bufferSize -= removedFrame.getSize();
        }

        notify();
    }

    private synchronized Frame getNextKeyFrame() throws InterruptedException
    {
        Frame nextFrame = null;

        while (nextFrame == null || !nextFrame.isKeyFrame())
        {
            if (frames.isEmpty())
                wait();

            nextFrame = frames.poll();
        }

        return nextFrame;
    }

    private synchronized Frame getNextFrame() throws InterruptedException
    {
        Frame nextFrame = null;

        while (nextFrame == null)
        {
            if (frames.isEmpty())
                wait();

            nextFrame = frames.poll();
        }

        return nextFrame;
    }

    public synchronized Frame getFrame() throws InterruptedException
    {
        if (this.nextKeyFrame) {
            this.nextKeyFrame = false;
            return getNextKeyFrame();
        }

        else
            return getNextFrame();
    }
}
