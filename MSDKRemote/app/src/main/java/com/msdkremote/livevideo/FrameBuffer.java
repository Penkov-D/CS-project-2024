package com.msdkremote.livevideo;

import java.util.LinkedList;
import java.util.Queue;

class FrameBuffer
{
    private final Queue<Frame> frames = new LinkedList<>();
    private final int maxBufferSize;
    private int bufferSize = 0;
    private boolean nextKeyFrame = true;
    private final int WAIT_TIMEOUT = 100;
    private final Object lock = new Object();

    public FrameBuffer(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }

    public int getMaxBufferSize() {
        return this.maxBufferSize;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public void nextKeyFrame() {
        synchronized (lock) {
            this.nextKeyFrame = true;
        }
    }

    public void addFrame(Frame frame)
    {
        synchronized (lock) {
            this.frames.add(frame);
            this.bufferSize += frame.getSize();

            while (this.bufferSize > this.maxBufferSize) {
                Frame removedFrame = this.frames.poll();
                this.nextKeyFrame = true;

                if (removedFrame == null)
                    this.bufferSize = 0;
                else
                    this.bufferSize -= removedFrame.getSize();
            }

            lock.notifyAll();
        }
    }

    private Frame getNextKeyFrame() throws InterruptedException
    {
        synchronized (lock) {
            Frame nextFrame = null;

            while (nextFrame == null || !nextFrame.isKeyFrame()) {
                if (frames.isEmpty())
                    lock.wait(WAIT_TIMEOUT);

                nextFrame = frames.poll();

                if (nextFrame == null)
                    this.bufferSize = 0;
                else
                    this.bufferSize -= nextFrame.getSize();
            }

            return nextFrame;
        }
    }

    private Frame getNextFrame() throws InterruptedException
    {
        synchronized (lock) {
            Frame nextFrame = null;

            while (nextFrame == null) {
                if (frames.isEmpty())
                    lock.wait(WAIT_TIMEOUT);

                nextFrame = frames.poll();

                if (nextFrame == null)
                    this.bufferSize = 0;
                else
                    this.bufferSize -= nextFrame.getSize();

            }

            return nextFrame;
        }
    }

    public Frame getFrame() throws InterruptedException
    {
        if (this.nextKeyFrame) {
            this.nextKeyFrame = false;
            return getNextKeyFrame();
        }

        else
            return getNextFrame();
    }
}
