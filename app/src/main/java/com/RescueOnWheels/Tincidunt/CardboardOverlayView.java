package com.RescueOnWheels.Tincidunt;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CardboardOverlayView extends LinearLayout {
    private final CardboardOverlayEyeView mLeftView;
    private final CardboardOverlayEyeView mRightView;

    public CardboardOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);

        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
        params.setMargins(0, 0, 0, 0);

        mLeftView = new CardboardOverlayEyeView(context, attrs);
        mLeftView.setLayoutParams(params);
        addView(mLeftView);

        mRightView = new CardboardOverlayEyeView(context, attrs);
        mRightView.setLayoutParams(params);
        addView(mRightView);

        // Set some reasonable defaults.
        setDepthOffset();
        setColor(Color.rgb(150, 255, 180));
        setVisibility(View.VISIBLE);

        AlphaAnimation mTextFadeAnimation = new AlphaAnimation(1.0f, 0.0f);
        mTextFadeAnimation.setDuration(2500);
    }

    public void show3DToast() {
        setTextAlpha(1f);
    }

    private void setDepthOffset() {
        mLeftView.setOffset((float) 0.016);
        mRightView.setOffset(-(float) 0.016);
    }

    private void setTextAlpha(float alpha) {
        mLeftView.setTextViewAlpha(alpha);
        mRightView.setTextViewAlpha(alpha);
    }

    private void setColor(int color) {
        mLeftView.setColor(color);
        mRightView.setColor(color);
    }

    public SurfaceView[] getSurfaceViews() {
        return new SurfaceView[]{mLeftView.getSurfaceView(), mRightView.getSurfaceView()};

    }

    public void setCallback(SurfaceHolder.Callback cb) {
        mLeftView.imageView.getHolder().addCallback(cb);
        mRightView.imageView.getHolder().addCallback(cb);

    }

    /**
     * A simple view group containing some horizontally centered text underneath a horizontally
     * centered image.
     * <p>
     * This is a helper class for CardboardOverlayView.
     */
    private class CardboardOverlayEyeView extends ViewGroup {
        private final SurfaceView imageView;
        private final TextView textView;
        private float offset;

        public CardboardOverlayEyeView(Context context, AttributeSet attrs) {
            super(context, attrs);
            imageView = new SurfaceView(context, attrs);
            addView(imageView);

            textView = new TextView(context, attrs);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f);
            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
            textView.setGravity(Gravity.CENTER);
            textView.setShadowLayer(3.0f, 0.0f, 0.0f, Color.DKGRAY);
            addView(textView);
        }

        void setColor(int color) {
            textView.setTextColor(color);
        }

        void setTextViewAlpha(float alpha) {
            textView.setAlpha(alpha);
        }

        void setOffset(float offset) {
            this.offset = offset;
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            // Width and height of this ViewGroup.
            final int width = right - left;
            final int height = bottom - top;

            // The size of the image, given as a fraction of the dimension as a ViewGroup. We multiply
            // both width and heading with this number to compute the image's bounding box. Inside the
            // box, the image is the horizontally and vertically centered.
            final float imageSize = 1.0f;

            // The fraction of this ViewGroup's height by which we shift the image off the ViewGroup's
            // center. Positive values shift downwards, negative values shift upwards.
            final float verticalImageOffset = -0.07f;

            // Vertical position of the text, specified in fractions of this ViewGroup's height.
            final float verticalTextPos = 0.52f;

            // Layout ImageView
            float imageMargin = (1.0f - imageSize) / 2.0f;
            float leftMargin = (int) (width * (imageMargin + offset));
            float topMargin = (int) (height * (imageMargin + verticalImageOffset));
            imageView.layout(
                    (int) leftMargin, (int) topMargin,
                    (int) (leftMargin + width * imageSize), (int) (topMargin + height * imageSize));

            // Layout TextView
            imageView.setZOrderMediaOverlay(true);
            leftMargin = offset * width;
            topMargin = height * verticalTextPos;
            textView.layout(
                    (int) leftMargin, (int) topMargin,
                    (int) (leftMargin + width), (int) (topMargin + height * (1.0f - verticalTextPos)));
        }

        SurfaceView getSurfaceView() {
            return imageView;
        }
    }
}