package com.appeaser.deckview.utilities;

import android.os.Handler;

/**
 * A dozer is a class that fires a trigger after it falls asleep.  You can occasionally poke it to
 * wake it up, but it will fall asleep if left untouched.
 *
 * <p>Source：https://github.com/vikramkakkar/DeckView
 */
public class DozeTrigger {

    private Handler mHandler;

    private boolean mIsDozing;
    private boolean mHasTriggered;
    private int mDozeDurationSeconds;
    private Runnable mSleepRunnable;

    // Sleep-runnable
    private Runnable mDozeRunnable = new Runnable() {
        @Override
        public void run() {
            mSleepRunnable.run();
            mIsDozing = false;
            mHasTriggered = true;
        }
    };

    public DozeTrigger(int dozeDurationSeconds, Runnable sleepRunnable) {
        mHandler = new Handler();
        mDozeDurationSeconds = dozeDurationSeconds;
        mSleepRunnable = sleepRunnable;
    }

    /**
     * Starts dozing. This also resets the trigger flag.
     */
    public void startDozing() {
        forcePoke();
        mHasTriggered = false;
    }

    /**
     * Stops dozing.
     */
    public void stopDozing() {
        mHandler.removeCallbacks(mDozeRunnable);
        mIsDozing = false;
    }

    /**
     * Poke this dozer to wake it up for a little bit, if it is dozing.
     */
    public void poke() {
        if (mIsDozing) {
            forcePoke();
        }
    }

    /**
     * Poke this dozer to wake it up for a little bit.
     */
    private void forcePoke() {
        mHandler.removeCallbacks(mDozeRunnable);
        mHandler.postDelayed(mDozeRunnable, mDozeDurationSeconds * 1000);
        mIsDozing = true;
    }

    /**
     * Returns whether we are dozing or not.
     */
    public boolean isDozing() {
        return mIsDozing;
    }

    /**
     * Returns whether the trigger has fired at least once.
     */
    public boolean hasTriggered() {
        return mHasTriggered;
    }

    /**
     * Resets the doze trigger state.
     */
    public void resetTrigger() {
        mHasTriggered = false;
    }
}
