package com.appeaser.deckview.utilities;

import java.util.ArrayList;

/**
 * A ref counted trigger that does some logic when the count is first incremented, or last
 * decremented.  Not thread safe as it's not currently needed.
 *
 * <p>Source：https://github.com/vikramkakkar/DeckView
 */
public class ReferenceCountedTrigger {

    private int mCount;
    private ArrayList<Runnable> mFirstIncRunnable = new ArrayList<>();
    private ArrayList<Runnable> mLastDecRunnable = new ArrayList<>();
    private Runnable mErrorRunnable;

    private Runnable mDecrementRunnable = new Runnable() {
        @Override
        public void run() {
            decrement();
        }
    };

    public ReferenceCountedTrigger(Runnable firstIncRunnable, Runnable lastDecRunnable, Runnable errorRunnable) {
        if (firstIncRunnable != null) mFirstIncRunnable.add(firstIncRunnable);
        if (lastDecRunnable != null) mLastDecRunnable.add(lastDecRunnable);
        mErrorRunnable = errorRunnable;
    }

    /**
     * Increments the ref count
     */
    public void increment() {
        if (mCount == 0 && !mFirstIncRunnable.isEmpty()) {
            int numRunnables = mFirstIncRunnable.size();
            for (int i = 0; i < numRunnables; i++) {
                mFirstIncRunnable.get(i).run();
            }
        }
        mCount++;
    }

    /**
     * Adds a runnable to the last-decrement runnables list.
     */
    public void addLastDecrementRunnable(Runnable r) {
        // To ensure that the last decrement always calls, we increment and decrement after setting
        // the last decrement runnable
        boolean ensureLastDecrement = (mCount == 0);
        if (ensureLastDecrement) increment();
        mLastDecRunnable.add(r);
        if (ensureLastDecrement) decrement();
    }

    /**
     * Decrements the ref count
     */
    public void decrement() {
        mCount--;
        if (mCount == 0 && !mLastDecRunnable.isEmpty()) {
            int numRunnables = mLastDecRunnable.size();
            for (int i = 0; i < numRunnables; i++) {
                mLastDecRunnable.get(i).run();
            }
        } else if (mCount < 0) {
            if (mErrorRunnable != null) {
                mErrorRunnable.run();
            } else {
                new Throwable("Invalid ref count").printStackTrace();
                //Console.logError(mContext, "Invalid ref count");
            }
        }
    }

    /**
     * Convenience method to decrement this trigger as a runnable.
     */
    public Runnable decrementAsRunnable() {
        return mDecrementRunnable;
    }

}
