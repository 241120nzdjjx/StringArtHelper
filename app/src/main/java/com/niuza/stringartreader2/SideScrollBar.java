/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 */
package com.niuza.stringartreader2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

/** A narrow draggable scrollbar for dialogs whose content also handles drag gestures. */
@android.annotation.SuppressLint("ViewConstructor")
final class SideScrollBar extends View {
    private final ScrollView target;
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;
    private float dragOffset;

    SideScrollBar(Context context, ScrollView scrollView) {
        super(context);
        target = scrollView;
        density = getResources().getDisplayMetrics().density;
        trackPaint.setColor(0x385F5F69);
        thumbPaint.setColor(0xFF7FD0CB);
        setContentDescription("Scroll");
        target.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override public void onScrollChange(View v, int scrollX, int scrollY,
                                                 int oldScrollX, int oldScrollY) {
                invalidate();
            }
        });
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Geometry geometry = geometry();
        if (geometry == null) return;
        float centerX = getWidth() * .5f;
        trackPaint.setStrokeWidth(Math.max(2f, 2f * density));
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        thumbPaint.setStrokeWidth(Math.max(5f, 5f * density));
        thumbPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(centerX, geometry.top, centerX, geometry.bottom, trackPaint);
        canvas.drawLine(centerX, geometry.thumbTop, centerX, geometry.thumbBottom, thumbPaint);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        Geometry geometry = geometry();
        if (geometry == null) return false;
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            getParent().requestDisallowInterceptTouchEvent(true);
            if (event.getY() >= geometry.thumbTop && event.getY() <= geometry.thumbBottom)
                dragOffset = event.getY() - geometry.thumbTop;
            else
                dragOffset = geometry.thumbHeight * .5f;
            scrollToTouch(event.getY(), geometry);
            return true;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            scrollToTouch(event.getY(), geometry);
            return true;
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            getParent().requestDisallowInterceptTouchEvent(false);
            if (action == MotionEvent.ACTION_UP) performClick();
            return true;
        }
        return true;
    }

    @Override public boolean performClick() {
        super.performClick();
        return true;
    }

    private void scrollToTouch(float y, Geometry geometry) {
        float available = geometry.trackHeight - geometry.thumbHeight;
        if (available <= 0f) return;
        float thumbTop = Math.max(geometry.top,
                Math.min(geometry.top + available, y - dragOffset));
        float fraction = (thumbTop - geometry.top) / available;
        target.scrollTo(0, Math.round(fraction * geometry.maxScroll));
        invalidate();
    }

    private Geometry geometry() {
        if (target.getChildCount() == 0 || target.getHeight() <= 0 || getHeight() <= 0)
            return null;
        // Portrait already scrolls naturally from the surrounding content. Keep the
        // extra handle landscape-only so the finished portrait layout stays unchanged.
        if (target.getWidth() <= target.getHeight()) return null;
        int contentHeight = target.getChildAt(0).getHeight();
        int viewportHeight = target.getHeight();
        int maxScroll = Math.max(0, contentHeight - viewportHeight);
        if (maxScroll == 0) return null;
        float padding = 10f * density;
        float top = padding;
        float bottom = Math.max(top, getHeight() - padding);
        float trackHeight = bottom - top;
        float thumbHeight = Math.max(36f * density,
                trackHeight * viewportHeight / Math.max(1f, contentHeight));
        thumbHeight = Math.min(trackHeight, thumbHeight);
        float available = trackHeight - thumbHeight;
        float fraction = Math.max(0f, Math.min(1f,
                target.getScrollY() / (float) maxScroll));
        float thumbTop = top + available * fraction;
        return new Geometry(top, bottom, trackHeight, thumbHeight,
                thumbTop, thumbTop + thumbHeight, maxScroll);
    }

    private static final class Geometry {
        final float top;
        final float bottom;
        final float trackHeight;
        final float thumbHeight;
        final float thumbTop;
        final float thumbBottom;
        final int maxScroll;

        Geometry(float top, float bottom, float trackHeight, float thumbHeight,
                 float thumbTop, float thumbBottom, int maxScroll) {
            this.top = top;
            this.bottom = bottom;
            this.trackHeight = trackHeight;
            this.thumbHeight = thumbHeight;
            this.thumbTop = thumbTop;
            this.thumbBottom = thumbBottom;
            this.maxScroll = maxScroll;
        }
    }
}
