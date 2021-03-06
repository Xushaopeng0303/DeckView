package com.appeaser.deckview.views;

import android.graphics.Outline;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.appeaser.deckview.helpers.DeckViewConfig;

/**
 * An outline provider that has a clip and outline that can be animated.
 *
 * <p>Source：https://github.com/vikramkakkar/DeckView
 */
class AnimateableDeckChildViewBounds extends ViewOutlineProvider {

    private DeckViewConfig mConfig;

    private DeckChildView mSourceView;
    private Rect mClipRect = new Rect();
    private Rect mClipBounds = new Rect();
    private int mCornerRadius;
    private float mAlpha = 1f;
    private static final float MIN_ALPHA = 0.25f;

    AnimateableDeckChildViewBounds(DeckChildView source, int cornerRadius) {
        mConfig = DeckViewConfig.getInstance();
        mSourceView = source;
        mCornerRadius = cornerRadius;
        setClipBottom(getClipBottom());
    }

    @Override
    public void getOutline(View view, Outline outline) {
        outline.setAlpha(MIN_ALPHA + mAlpha / (1f - MIN_ALPHA));
        outline.setRoundRect(mClipRect.left, mClipRect.top,
                mSourceView.getWidth() - mClipRect.right,
                mSourceView.getHeight() - mClipRect.bottom,
                mCornerRadius);
    }

    /**
     * Sets the view outline alpha.
     */
    void setAlpha(float alpha) {
        if (Float.compare(alpha, mAlpha) != 0) {
            mAlpha = alpha;
            mSourceView.invalidateOutline();
        }
    }

    /**
     * Sets the bottom clip.
     */
    void setClipBottom(int bottom) {
        if (bottom != mClipRect.bottom) {
            mClipRect.bottom = bottom;
            mSourceView.invalidateOutline();
            updateClipBounds();
            if (!mConfig.useHardwareLayers) {
                mSourceView.mThumbnailView.updateThumbnailVisibility(
                        bottom - mSourceView.getPaddingBottom());
            }
        }
    }

    /**
     * Returns the bottom clip.
     */
    private int getClipBottom() {
        return mClipRect.bottom;
    }

    private void updateClipBounds() {
        mClipBounds.set(mClipRect.left, mClipRect.top,
                mSourceView.getWidth() - mClipRect.right,
                mSourceView.getHeight() - mClipRect.bottom);
        mSourceView.setClipBounds(mClipBounds);
    }
}
