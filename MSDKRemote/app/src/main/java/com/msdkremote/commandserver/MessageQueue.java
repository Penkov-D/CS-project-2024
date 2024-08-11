package com.msdkremote.commandserver;

import androidx.annotation.NonNull;

import java.util.LinkedList;
import java.util.Queue;

class MessageQueue
{
    //TODO: add method for removing command, as it may be read, but yet to process.

    // The maximum capacity this queue will hold
    private final int capacity;

    // The actual storage object
    private final Queue<String> pendingQueue = new LinkedList<>();


    /**
     * Creates new MessageQueue object, without limit on its capacity.
     */
    public MessageQueue() {
        this.capacity = Integer.MAX_VALUE;
    }


    /**
     * Creates new MessageQueue object that cna hold only <strong>capacity</strong> messages.
     * Useful if the using object is very verbose to moderate memory usage.
     *
     * @param capacity the maximum number of messages that this queue can hold.
     */
    public MessageQueue(int capacity)
    {
        // The capacity mast be positive, as something should be stored
        if (capacity < 1)
            throw new IllegalArgumentException("Capacity cannot be negative.");

        this.capacity = capacity;
    }


    /**
     * Gets the number of messages pending in this queue.
     *
     * @return the number of messages in this queue.
     */
    public synchronized int getSize() {
        return pendingQueue.size();
    }


    /**
     * Adds <strong>message</strong> to the top of the queue.
     * <p>
     * Note that if the the queue is about to exceed its allowed capacity,
     * it will remove the oldest message out.
     *
     * @param message message to add to the queue.
     */
    public synchronized void addMessage(String message)
    {
        // Add message to the queue
        pendingQueue.add(message);

        // Remove message if the queue is overflowed.
        // Note that as this the only function to add message and is synchronized,
        // there can be only one extra message on the queue.
        if (pendingQueue.size() > capacity)
            pendingQueue.remove();

        // Wakeup the threads that waits for a message
        // Because both this and the getMessage methods use the same lock (this class),
        // it is guaranteed that no one will wait while the queue is not empty.
        notifyAll();
    }


    /**
     * Gets message from the queue.
     * None-blocking method.
     *
     * @return the oldest message in the queue, or null if the queue is empty.
     */
    public synchronized String getMessage() {
        // pull() will return null of queue is empty.
        return pendingQueue.poll();
    }


    /**
     * Gets message from the queue.
     * Blocking method, with or without timeout.
     *
     * @param timeout_ms the minimum time to wait, or wait indefinitely if equal zero.
     * @return the oldest message from the queue,
     *   or null if timeout reached and no new messages appended.
     * @throws InterruptedException if the current thread was interrupted while waiting.
     */
    public String getMessage(long timeout_ms) throws InterruptedException
    {
        // Timeout cannot be negative
        if (timeout_ms < 0)
            throw new InterruptedException("timeout cannot be negative.");

        // If timeout equal zero, wait without timeout
        if (timeout_ms == 0)
            return getMessageBlocking();

        // Convert timeout time to end time.
        return getMessageTimeout(System.currentTimeMillis() + timeout_ms);
    }


    /**
     * Get message with a specific time to wait until.
     *
     * @param endTime system end time that this method allowed to wait
     * @return oldest message or null if timeout
     * @throws InterruptedException if the current thread was interrupted while waiting.
     */
    private synchronized String getMessageTimeout(long endTime) throws InterruptedException
    {
        long currentTime;

        // Wait only if the queue is empty and end time wasn't reached.
        // Using variable for currentTime makes sure that wait always will gets positive value.
        while (pendingQueue.isEmpty() && (currentTime = System.currentTimeMillis()) < endTime)
            wait(endTime - currentTime);

        // pull() will return null of queue is empty.
        return pendingQueue.poll();
    }

    /**
     * Waiting indefinitely for a message.
     *
     * @return oldest message in the queue.
     * @throws InterruptedException if the current thread was interrupted while waiting.
     */
    private synchronized @NonNull String getMessageBlocking() throws InterruptedException
    {
        // Wait until the queue has message to offer.
        while (pendingQueue.isEmpty())
            wait();

        // Will throw exception if no message.
        // Should not happen if the implementation is thread safe.
        return pendingQueue.remove();
    }
}
