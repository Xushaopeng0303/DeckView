package com.appeaser.deckview.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.appeaser.deckview.R;

/**
 * Configuration helper
 *
 * <p>Source：https://github.com/vikramkakkar/DeckView
 */
public class DeckViewConfig {
    private static DeckViewConfig sInstance;
    private static int sPrevConfigurationHashCode;

    /** Animations */
    public float animationPxMovementPerSecond;

    /** Interpolators */
    public Interpolator fastOutSlowInInterpolator;
    public Interpolator fastOutLinearInInterpolator;
    public Interpolator linearOutSlowInInterpolator;
    public Interpolator quintOutInterpolator;

    /** Insets */
    public Rect systemInsets = new Rect();
    private Rect displayRect = new Rect();


    /** Task stack */
    public int taskStackScrollDuration;
    public int taskStackMaxDim;
    public int taskStackTopPaddingPx;
    public float taskStackWidthPaddingPct;
    public float taskStackOverscrollPct;

    /** Transitions */
    public int transitionEnterFromHomeDelay;

    /** Task view animation and styles */
    public int taskViewEnterFromAppDuration;
    public int taskViewEnterFromHomeDuration;
    public int taskViewEnterFromHomeStaggerDelay;
    public int taskViewExitToAppDuration;
    public int taskViewExitToHomeDuration;
    public int taskViewRemoveAnimDuration;
    public int taskViewRemoveAnimTranslationXPx;
    public int taskViewTranslationZMinPx;
    public int taskViewTranslationZMaxPx;
    public int taskViewRoundedCornerRadiusPx;
    public int taskViewHighlightPx;
    public int taskViewAffiliateGroupEnterOffsetPx;
    public float taskViewThumbnailAlpha;

    /** Task bar colors */
    public int taskBarViewLightTextColor;
    public int taskBarViewHighlightColor;

    /** Task bar size & animations */
    public int taskBarHeight;
    public int taskBarDismissDozeDelaySeconds;

    /**
     * Launch states
     */
    public boolean launchedWithAltTab;
    public boolean launchedFromAppWithThumbnail;
    public boolean launchedFromHome;
    public boolean launchedHasConfigurationChanged;

    /**
     * Misc *
     */
    public boolean useHardwareLayers;
    public boolean fakeShadows;

    /**
     * Private constructor
     */
    private DeckViewConfig(Context context) {
        // Properties that don't have to be reloaded with each configuration change can be loaded
        // here.

        // Interpolators
        fastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context,
                R.interpolator.fast_out_slow_in);
        fastOutLinearInInterpolator = AnimationUtils.loadInterpolator(context,
                R.interpolator.fast_out_linear_in);
        linearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context,
                R.interpolator.linear_out_slow_in);
        quintOutInterpolator = AnimationUtils.loadInterpolator(context,
                R.interpolator.decelerate_quint);
    }

    /**
     * Updates the configuration to the current context
     */
    public static DeckViewConfig reinitialize(Context context) {
        if (sInstance == null) {
            sInstance = new DeckViewConfig(context);
        }
        int configHashCode = context.getResources().getConfiguration().hashCode();
        if (sPrevConfigurationHashCode != configHashCode) {
            sInstance.update(context);
            sPrevConfigurationHashCode = configHashCode;
        }

        sInstance.updateOnReinitialize(context);
        return sInstance;
    }

    /**
     * Returns the current recents configuration
     */
    public static DeckViewConfig getInstance() {
        return sInstance;
    }

    /**
     * Updates the state, given the specified context
     */
    private void update(Context context) {
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();

        // Insets
        displayRect.set(0, 0, dm.widthPixels, dm.heightPixels);

        // Animations
        animationPxMovementPerSecond =
                res.getDimensionPixelSize(R.dimen.animation_movement_in_dps_per_second);

        // Task stack
        taskStackScrollDuration =
                res.getInteger(R.integer.animate_deck_scroll_duration);
        TypedValue widthPaddingPctValue = new TypedValue();
        res.getValue(R.dimen.deck_width_padding_percentage, widthPaddingPctValue, true);
        taskStackWidthPaddingPct = widthPaddingPctValue.getFloat();
        TypedValue stackOverscrollPctValue = new TypedValue();
        res.getValue(R.dimen.deck_overscroll_percentage, stackOverscrollPctValue, true);
        taskStackOverscrollPct = stackOverscrollPctValue.getFloat();
        taskStackMaxDim = res.getInteger(R.integer.max_deck_view_dim);
        taskStackTopPaddingPx = res.getDimensionPixelSize(R.dimen.deck_top_padding);

        // Transition
        transitionEnterFromHomeDelay =
                res.getInteger(R.integer.enter_from_home_transition_duration);

        // Task view animation and styles
        taskViewEnterFromAppDuration =
                res.getInteger(R.integer.task_enter_from_app_duration);
        taskViewEnterFromHomeDuration =
                res.getInteger(R.integer.task_enter_from_home_duration);
        taskViewEnterFromHomeStaggerDelay =
                res.getInteger(R.integer.task_enter_from_home_stagger_delay);
        taskViewExitToAppDuration =
                res.getInteger(R.integer.task_exit_to_app_duration);
        taskViewExitToHomeDuration =
                res.getInteger(R.integer.task_exit_to_home_duration);
        taskViewRemoveAnimDuration =
                res.getInteger(R.integer.animate_task_view_remove_duration);
        taskViewRemoveAnimTranslationXPx =
                res.getDimensionPixelSize(R.dimen.task_view_remove_anim_translation_x);
        taskViewRoundedCornerRadiusPx =
                res.getDimensionPixelSize(R.dimen.task_view_rounded_corners_radius);
        taskViewHighlightPx = res.getDimensionPixelSize(R.dimen.task_view_highlight);
        taskViewTranslationZMinPx = res.getDimensionPixelSize(R.dimen.task_view_z_min);
        taskViewTranslationZMaxPx = res.getDimensionPixelSize(R.dimen.task_view_z_max);
        taskViewAffiliateGroupEnterOffsetPx =
                res.getDimensionPixelSize(R.dimen.task_view_affiliate_group_enter_offset);
        TypedValue thumbnailAlphaValue = new TypedValue();
        res.getValue(R.dimen.task_view_thumbnail_alpha, thumbnailAlphaValue, true);
        taskViewThumbnailAlpha = thumbnailAlphaValue.getFloat();

        // Task bar colors
        taskBarViewLightTextColor =
                res.getColor(R.color.task_bar_light_text_color);
        taskBarViewHighlightColor =
                res.getColor(R.color.task_bar_highlight_color);
        TypedValue affMinAlphaPctValue = new TypedValue();
        res.getValue(R.dimen.task_affiliation_color_min_alpha_percentage, affMinAlphaPctValue, true);

        // Task bar size & animations
        taskBarHeight = res.getDimensionPixelSize(R.dimen.deck_child_header_bar_height);
        taskBarDismissDozeDelaySeconds =
                res.getInteger(R.integer.task_bar_dismiss_delay_seconds);

        // Misc
        useHardwareLayers = res.getBoolean(R.bool.config_use_hardware_layers);
        fakeShadows = res.getBoolean(R.bool.config_fake_shadows);
    }

    /**
     * Updates the system insets
     */
    public void updateSystemInsets(Rect insets) {
        systemInsets.set(insets);
    }

    /**
     * Updates the states that need to be re-read whenever we re-initialize.
     */
    private void updateOnReinitialize(Context context/*, SystemServicesProxy ssp*/) {

    }

    /**
     * Returns the task stack bounds in the current orientation. These bounds do not account for
     * the system insets.
     */
    public void getTaskStackBounds(int windowWidth, int windowHeight, int topInset, int rightInset,
                                   Rect taskStackBounds) {
        taskStackBounds.set(0, 0, windowWidth, windowHeight);
    }
}