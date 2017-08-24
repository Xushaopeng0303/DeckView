package com.appeaser.deckview.views;

import android.animation.ValueAnimator;
import android.graphics.Rect;

import com.appeaser.deckview.helpers.DeckChildViewTransform;
import com.appeaser.deckview.utilities.ReferenceCountedTrigger;

/**
 * Common code related to view animations
 * <p>
 * <p>Source：https://github.com/vikramkakkar/DeckView
 */
class ViewAnimation {

    /**
     * The animation context for a task view animation into recent context
     */
    static class TaskViewEnterContext {
        // A trigger to run some logic when all the animations complete.  This works around the fact
        // that it is difficult to coordinate ViewPropertyAnimators
        ReferenceCountedTrigger postAnimationTrigger;
        // An update listener to notify as the enter animation progresses (used for the home transition)
        ValueAnimator.AnimatorUpdateListener updateListener;

        // These following properties are updated for each task view we start the enter animation on

        // Whether or not the current task occludes the launch target
        boolean currentTaskOccludesLaunchTarget;
        // The task rect for the current stack
        Rect currentTaskRect;
        // The transform of the current task view
        DeckChildViewTransform currentTaskTransform;
        // The view index of the current task view
        int currentStackViewIndex;
        // The total number of task views
        int currentStackViewCount;

        TaskViewEnterContext(ReferenceCountedTrigger t) {
            postAnimationTrigger = t;
        }
    }

    /**
     * The animation context for a task view animation out of recent context
     */
    static class TaskViewExitContext {
        // A trigger to run some logic when all the animations complete.  This works around the fact
        // that it is difficult to coordinate ViewPropertyAnimators
        ReferenceCountedTrigger postAnimationTrigger;

        // The translationY to apply to a TaskView to move it off the bottom of the task stack
        int offscreenTranslationY;

        TaskViewExitContext(ReferenceCountedTrigger t) {
            postAnimationTrigger = t;
        }
    }

}
