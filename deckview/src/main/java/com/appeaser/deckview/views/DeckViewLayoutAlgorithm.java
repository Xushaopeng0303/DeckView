package com.appeaser.deckview.views;

import android.graphics.Rect;

import com.appeaser.deckview.helpers.DeckChildViewTransform;
import com.appeaser.deckview.helpers.DeckViewConfig;
import com.appeaser.deckview.utilities.DVUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The layout logic for a TaskStackView.
 *
 * <p>We are using a curve that defines the curve of the tasks as that go back in the recents list.
 * The curve is defined such that at curve progress p = 0 is the end of the curve (the top of the
 * stack rect), and p = 1 at the start of the curve and the bottom of the stack rect.
 *
 * <p>Source：https://github.com/vikramkakkar/DeckView
 */
class DeckViewLayoutAlgorithm<T> {

    // These are all going to change，The min scale of the last card in the peek area
    private static final float STACK_PEEK_MIN_SCALE = 0.8f;

    // A report of the visibility state of the stack
    private class VisibilityReport {
        int numVisibleTasks;
        int numVisibleThumbnails;

        /**
         * Package level ctor
         */
        VisibilityReport(int tasks, int thumbnails) {
            numVisibleTasks = tasks;
            numVisibleThumbnails = thumbnails;
        }
    }

    private DeckViewConfig mConfig;

    // The various rects that define the stack view
    Rect mViewRect = new Rect();
    Rect mStackVisibleRect = new Rect();
    private Rect mStackRect = new Rect();
    Rect mTaskRect = new Rect();

    // The min/max scroll progress
    float mMinScrollP;
    float mMaxScrollP;
    float mInitialScrollP;
    private int mBetweenAffiliationOffset;
    private HashMap<T, Float> mTaskProgressMap = new HashMap<>();

    // Log function，The large the X_SCALE, the longer the flat area of the curve
    private static final float X_SCALE = 1.75f;
    private static final float LOG_BASE = 3000;
    private static final int PRECISION_STEPS = 250;
    private static float[] xp;
    private static float[] px;

    DeckViewLayoutAlgorithm(DeckViewConfig config) {
        mConfig = config;

        initializeCurve();
    }

    /**
     * Computes the stack and task rects
     */
    void computeRect(int windowWidth, int windowHeight, Rect taskStackBounds) {
        // Compute the stack rect
        mViewRect.set(0, 0, windowWidth, windowHeight);
        mStackRect.set(taskStackBounds);
        mStackVisibleRect.set(taskStackBounds);
        mStackVisibleRect.bottom = mViewRect.bottom;

        int widthPadding = (int) (mConfig.taskStackWidthPaddingPct * mStackRect.width());
        int heightPadding = mConfig.taskStackTopPaddingPx;
        mStackRect.inset(widthPadding, heightPadding);

        // Compute the task rect
        int size = mStackRect.width();
        int left = mStackRect.left + (mStackRect.width() - size) / 2;
        mTaskRect.set(left, mStackRect.top,
                left + size, mStackRect.top + size);

        // Update the affiliation offsets
        float visibleTaskPct = 0.5f;
        mBetweenAffiliationOffset = (int) (visibleTaskPct * mTaskRect.height());
    }

    /**
     * Computes the minimum and maximum scroll progress values.  This method may be called before
     * the RecentsConfiguration is set, so we need to pass in the alt-tab state.
     */
    void computeMinMaxScroll(ArrayList<T> data, boolean launchedWithAltTab,
                             boolean launchedFromHome) {
        // Clear the progress map
        mTaskProgressMap.clear();

        // Return early if we have no tasks
        if (data.isEmpty()) {
            mMinScrollP = mMaxScrollP = 0;
            return;
        }

        // Note that we should account for the scale difference of the offsets at the screen bottom
        int taskHeight = mTaskRect.height();
        float pAtBottomOfStackRect = screenYToCurveProgress(mStackVisibleRect.bottom);
        float pBetweenAffiliateOffset = pAtBottomOfStackRect -
                screenYToCurveProgress(mStackVisibleRect.bottom - mBetweenAffiliationOffset);
        float pTaskHeightOffset = pAtBottomOfStackRect -
                screenYToCurveProgress(mStackVisibleRect.bottom - taskHeight);
        float pNavBarOffset = pAtBottomOfStackRect -
                screenYToCurveProgress(mStackVisibleRect.bottom - (mStackVisibleRect.bottom -
                        mStackRect.bottom));

        // Update the task offsets
        float pAtFrontMostCardTop = 0.5f;
        int taskCount = data.size();
        for (int i = 0; i < taskCount; i++) {
            //Task task = tasks.get(i);
            //mTaskProgressMap.put(task.key, pAtFrontMostCardTop);
            mTaskProgressMap.put(data.get(i), pAtFrontMostCardTop);

            if (i < (taskCount - 1)) {
                // Increment the peek height
                // TODO: Might need adjustments
                //float pPeek = task.group.isFrontMostTask(task) ?
                //pBetweenAffiliateOffset : pWithinAffiliateOffset;
                pAtFrontMostCardTop += pBetweenAffiliateOffset;
            }
        }

        mMaxScrollP = pAtFrontMostCardTop - ((1f - pTaskHeightOffset - pNavBarOffset));
        mMinScrollP = data.size() == 1 ? Math.max(mMaxScrollP, 0f) : 0f;
        if (launchedWithAltTab && launchedFromHome) {
            // Center the top most task, since that will be focused first
            mInitialScrollP = mMaxScrollP;
        } else {
            mInitialScrollP = pAtFrontMostCardTop - 0.825f;
        }
        mInitialScrollP = Math.min(mMaxScrollP, Math.max(0, mInitialScrollP));
    }

    /**
     * Computes the maximum number of visible tasks and thumbnails.  Requires that
     * computeMinMaxScroll() is called first.
     */
    VisibilityReport computeStackVisibilityReport(ArrayList<T> data) {
        if (data.size() <= 1) {
            return new VisibilityReport(1, 1);
        }

        // Walk backwards in the task stack and count the number of tasks and visible thumbnails
        int taskHeight = mTaskRect.height();
        int numVisibleTasks = 1;
        int numVisibleThumbnails = 1;
        //float progress = mTaskProgressMap.get(tasks.get(tasks.size() - 1).key) - mInitialScrollP;

        float progress = mTaskProgressMap.get(data.get(data.size() - 1)) - mInitialScrollP;
        int prevScreenY = curveProgressToScreenY(progress);
        for (int i = data.size() - 2; i >= 0; i--) {
            //Task task = tasks.get(i);
            //progress = mTaskProgressMap.get(task.key) - mInitialScrollP;
            progress = mTaskProgressMap.get(data.get(i)) - mInitialScrollP;
            if (progress < 0) {
                break;
            }

            // TODO: Might need adjustments
            //boolean isFrontMostTaskInGroup = task.group.isFrontMostTask(task);
            boolean isFrontMostTaskInGroup = true;
            if (isFrontMostTaskInGroup) {
                float scaleAtP = curveProgressToScale(progress);
                int scaleYOffsetAtP = (int) (((1f - scaleAtP) * taskHeight) / 2);
                int screenY = curveProgressToScreenY(progress) + scaleYOffsetAtP;
                boolean hasVisibleThumbnail = (prevScreenY - screenY) > mConfig.taskBarHeight;
                if (hasVisibleThumbnail) {
                    numVisibleThumbnails++;
                    numVisibleTasks++;
                    prevScreenY = screenY;
                } else {
                    // Once we hit the next front most task that does not have a visible thumbnail,
                    // walk through remaining visible set
                    for (int j = i; j >= 0; j--) {
                        numVisibleTasks++;
                        progress = mTaskProgressMap.get(data.get(i)) - mInitialScrollP;
                        if (progress < 0) {
                            break;
                        }
                    }
                    break;
                }
            } else if (!isFrontMostTaskInGroup) {
                // Affiliated task, no thumbnail
                numVisibleTasks++;
            }
        }
        return new VisibilityReport(numVisibleTasks, numVisibleThumbnails);
    }

    /**
     * Update/get the transform
     */
    DeckChildViewTransform getStackTransform(T key, float stackScroll,
                                                    DeckChildViewTransform transformOut,
                                                    DeckChildViewTransform prevTransform) {
        // Return early if we have an invalid index
        if (!mTaskProgressMap.containsKey(key)) {
            transformOut.reset();
            return transformOut;
        }
        return getStackTransform(mTaskProgressMap.get(key), stackScroll, transformOut,
                prevTransform);
    }

    /**
     * Update/get the transform
     */
    DeckChildViewTransform getStackTransform(float taskProgress, float stackScroll,
                                                    DeckChildViewTransform transformOut,
                                                    DeckChildViewTransform prevTransform) {
        float pTaskRelative = taskProgress - stackScroll;
        float pBounded = Math.max(0, Math.min(pTaskRelative, 1f));
        // If the task top is outside of the bounds below the screen, then immediately reset it
        if (pTaskRelative > 1f) {
            transformOut.reset();
            transformOut.rect.set(mTaskRect);
            return transformOut;
        }
        // The check for the top is trickier, since we want to show the next task if it is at all
        // visible, even if p < 0.
        if (pTaskRelative < 0f) {
            if (prevTransform != null && Float.compare(prevTransform.p, 0f) <= 0) {
                transformOut.reset();
                transformOut.rect.set(mTaskRect);
                return transformOut;
            }
        }
        float scale = curveProgressToScale(pBounded);
        int scaleYOffset = (int) (((1f - scale) * mTaskRect.height()) / 2);
        int minZ = mConfig.taskViewTranslationZMinPx;
        int maxZ = mConfig.taskViewTranslationZMaxPx;
        transformOut.scale = scale;
        transformOut.translationY = curveProgressToScreenY(pBounded) - mStackVisibleRect.top -
                scaleYOffset;
        transformOut.translationZ = Math.max(minZ, minZ + (pBounded * (maxZ - minZ)));
        transformOut.rect.set(mTaskRect);
        transformOut.rect.offset(0, transformOut.translationY);
        DVUtils.scaleRectAboutCenter(transformOut.rect, transformOut.scale);
        transformOut.visible = true;
        transformOut.p = pTaskRelative;
        return transformOut;
    }

    /**
     * Returns the scroll to such task top = 1f;
     */
    float getStackScrollForTask(T key) {
        if (!mTaskProgressMap.containsKey(key)) {
            return 0f;
        }
        return mTaskProgressMap.get(key);
    }

    /**
     * Initializes the curve.
     */
    private static void initializeCurve() {
        if (xp != null && px != null) {
            return;
        }
        xp = new float[PRECISION_STEPS + 1];
        px = new float[PRECISION_STEPS + 1];

        // Approximate f(x)
        float[] fx = new float[PRECISION_STEPS + 1];
        float step = 1f / PRECISION_STEPS;
        float x = 0;
        for (int xStep = 0; xStep <= PRECISION_STEPS; xStep++) {
            fx[xStep] = logFunc(x);
            x += step;
        }
        // Calculate the arc length for x:1->0
        float pLength = 0;
        float[] dx = new float[PRECISION_STEPS + 1];
        dx[0] = 0;
        for (int xStep = 1; xStep < PRECISION_STEPS; xStep++) {
            dx[xStep] = (float) Math.sqrt(Math.pow(fx[xStep] - fx[xStep - 1], 2) + Math.pow(step, 2));
            pLength += dx[xStep];
        }
        // Approximate p(x), a function of cumulative progress with x, normalized to 0..1
        float p = 0;
        px[0] = 0f;
        px[PRECISION_STEPS] = 1f;
        for (int xStep = 1; xStep <= PRECISION_STEPS; xStep++) {
            p += Math.abs(dx[xStep] / pLength);
            px[xStep] = p;
        }
        // Given p(x), calculate the inverse function x(p). This assumes that x(p) is also a valid
        // function.
        int xStep = 0;
        p = 0;
        xp[0] = 0f;
        xp[PRECISION_STEPS] = 1f;
        for (int pStep = 0; pStep < PRECISION_STEPS; pStep++) {
            // Walk forward in px and find the x where px <= p && p < px+1
            while (xStep < PRECISION_STEPS) {
                if (px[xStep] > p) break;
                xStep++;
            }
            // Now, px[xStep-1] <= p < px[xStep]
            if (xStep == 0) {
                xp[pStep] = 0;
            } else {
                // Find x such that proportionally, x is correct
                float fraction = (p - px[xStep - 1]) / (px[xStep] - px[xStep - 1]);
                x = (xStep - 1 + fraction) * step;
                xp[pStep] = x;
            }
            p += step;
        }
    }

    /**
     * Reverses and scales out x.
     */
    private static float reverse(float x) {
        return (-x * X_SCALE) + 1;
    }

    /**
     * The log function describing the curve.
     */
    private static float logFunc(float x) {
        return 1f - (float) (Math.pow(LOG_BASE, reverse(x))) / (LOG_BASE);
    }

    /**
     * Converts from the progress along the curve to a screen coordinate.
     */
    private int curveProgressToScreenY(float p) {
        if (p < 0 || p > 1) {
            return mStackVisibleRect.top + (int) (p * mStackVisibleRect.height());
        }
        float pIndex = p * PRECISION_STEPS;
        int pFloorIndex = (int) Math.floor(pIndex);
        int pCeilIndex = (int) Math.ceil(pIndex);
        float xFraction = 0;
        if (pFloorIndex < PRECISION_STEPS && (pCeilIndex != pFloorIndex)) {
            float pFraction = (pIndex - pFloorIndex) / (pCeilIndex - pFloorIndex);
            xFraction = (xp[pCeilIndex] - xp[pFloorIndex]) * pFraction;
        }
        float x = xp[pFloorIndex] + xFraction;
        return mStackVisibleRect.top + (int) (x * mStackVisibleRect.height());
    }

    /**
     * Converts from the progress along the curve to a scale.
     */
    private float curveProgressToScale(float p) {
        if (p < 0) return STACK_PEEK_MIN_SCALE;
        if (p > 1) return 1f;
        float scaleRange = (1f - STACK_PEEK_MIN_SCALE);
        return STACK_PEEK_MIN_SCALE + (p * scaleRange);
    }

    /**
     * Converts from a screen coordinate to the progress along the curve.
     */
    float screenYToCurveProgress(int screenY) {
        float x = (float) (screenY - mStackVisibleRect.top) / mStackVisibleRect.height();
        if (x < 0 || x > 1) {
            return x;
        }
        float xIndex = x * PRECISION_STEPS;
        int xFloorIndex = (int) Math.floor(xIndex);
        int xCeilIndex = (int) Math.ceil(xIndex);
        float pFraction = 0;
        if (xFloorIndex < PRECISION_STEPS && (xCeilIndex != xFloorIndex)) {
            float xFraction = (xIndex - xFloorIndex) / (xCeilIndex - xFloorIndex);
            pFraction = (px[xCeilIndex] - px[xFloorIndex]) * xFraction;
        }
        return px[xFloorIndex] + pFraction;
    }
}