/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 */
package com.niuza.stringartreader2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

/** A small dependency-free cropper: drag to move; use two fingers to zoom. */
final class SquareCropView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint whitePaint = new Paint();
    private final Paint outsideMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path outsideMask = new Path();
    private final Rect sourceRect = new Rect();
    private final RectF destinationRect = new RectF();
    private Bitmap bitmap;
    private String hintText = "拖动取景 · 双指缩放";
    private float centerX = .5f, centerY = .5f, zoom = 1f;
    private float lastX, lastY, lastDistance, gestureZoom;
    private boolean pinching;
    private float activeSnapZoom = Float.NaN;
    private final long[] recentSnapTimes = new long[4];
    private int recentSnapCount;
    private long snappingPausedUntil;

    private static final float SNAP_DISTANCE_DP = 14f;
    private static final long SNAP_REPEAT_WINDOW_MS = 3000L;
    private static final long SNAP_PAUSE_MS = 3000L;

    SquareCropView(Context context) {
        super(context);
        whitePaint.setColor(Color.WHITE);
        outsideMaskPaint.setColor(0x99000000);
        setBackgroundColor(Color.BLACK);
    }
    void setBitmap(Bitmap value) {
        bitmap = value;
        centerX = centerY = .5f;
        zoom = StringArtGenerator.minimumCropZoom(value);
        invalidate();
    }
    void setHintText(String value) { hintText = value == null ? "" : value; invalidate(); }
    void setCrop(float x, float y, float valueZoom) {
        if (bitmap == null) return;
        centerX = x;
        centerY = y;
        zoom = StringArtGenerator.clampCropZoom(bitmap, valueZoom);
        clampCenter();
        invalidate();
    }
    float getCenterX() { return centerX; }
    float getCenterY() { return centerY; }
    float getZoom() { return zoom; }

    private float cropSizePx() { return StringArtGenerator.cropSizePx(bitmap, zoom); }
    private void clampCenter() {
        float crop = cropSizePx();
        if (crop >= bitmap.getWidth()) {
            centerX = .5f;
        } else {
            float halfX = crop / bitmap.getWidth() * .5f;
            centerX = Math.max(halfX, Math.min(1f - halfX, centerX));
        }
        if (crop >= bitmap.getHeight()) {
            centerY = .5f;
        } else {
            float halfY = crop / bitmap.getHeight() * .5f;
            centerY = Math.max(halfY, Math.min(1f - halfY, centerY));
        }
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap == null || getWidth() <= 0 || getHeight() <= 0) return;
        float crop = cropSizePx();
        float cropLeft = centerX * bitmap.getWidth() - crop * .5f;
        float cropTop = centerY * bitmap.getHeight() - crop * .5f;
        int sourceLeft = Math.max(0, (int) Math.floor(cropLeft));
        int sourceTop = Math.max(0, (int) Math.floor(cropTop));
        int sourceRight = Math.min(bitmap.getWidth(), (int) Math.ceil(cropLeft + crop));
        int sourceBottom = Math.min(bitmap.getHeight(), (int) Math.ceil(cropTop + crop));
        float side = Math.min(getWidth(), getHeight());
        float left = (getWidth() - side) * .5f;
        float top = (getHeight() - side) * .5f;
        float circleRadius = side * .5f * StringArtGenerator.TARGET_RADIUS_RATIO;
        canvas.drawRect(left, top, left + side, top + side, whitePaint);
        if (sourceRight > sourceLeft && sourceBottom > sourceTop) {
            sourceRect.set(sourceLeft, sourceTop, sourceRight, sourceBottom);
            destinationRect.set(
                    left + (sourceLeft - cropLeft) / crop * side,
                    top + (sourceTop - cropTop) / crop * side,
                    left + (sourceRight - cropLeft) / crop * side,
                    top + (sourceBottom - cropTop) / crop * side);
            canvas.drawBitmap(bitmap, sourceRect, destinationRect, paint);
        }
        outsideMask.reset();
        outsideMask.setFillType(Path.FillType.EVEN_ODD);
        outsideMask.addRect(0f, 0f, getWidth(), getHeight(), Path.Direction.CW);
        outsideMask.addCircle(getWidth() * .5f, getHeight() * .5f,
                circleRadius, Path.Direction.CW);
        canvas.drawPath(outsideMask, outsideMaskPaint);
        paint.setStyle(Paint.Style.STROKE); paint.setColor(Color.WHITE); paint.setStrokeWidth(2f);
        canvas.drawCircle(getWidth() * .5f, getHeight() * .5f, circleRadius, paint);
        paint.setColor(0xCCFFFFFF); paint.setTextSize(22f); paint.setStyle(Paint.Style.FILL); paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(hintText, getWidth() * .5f,
                Math.min(getHeight() - 14f, getHeight() * .5f + circleRadius - 14f), paint);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (bitmap == null) return false;
        if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN && event.getPointerCount() >= 2) {
            pinching = true;
            lastDistance = distance(event);
            gestureZoom = zoom;
            activeSnapZoom = Float.NaN;
            return true;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) { lastX = event.getX(); lastY = event.getY(); pinching = false; return true; }
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            if (pinching && event.getPointerCount() >= 2) {
                float d = distance(event);
                if (lastDistance > 0) {
                    gestureZoom = StringArtGenerator.clampCropZoom(
                            bitmap, gestureZoom * d / lastDistance);
                    zoom = snapZoomIfNear(gestureZoom);
                }
                lastDistance = d;
                clampCenter();
                invalidate();
                return true;
            }
            float scaleX = cropSizePx() / bitmap.getWidth();
            float scaleY = cropSizePx() / bitmap.getHeight();
            float side = Math.min(getWidth(), getHeight());
            centerX -= (event.getX() - lastX) / Math.max(1f, side) * scaleX;
            centerY -= (event.getY() - lastY) / Math.max(1f, side) * scaleY;
            lastX = event.getX(); lastY = event.getY(); clampCenter(); invalidate(); return true;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP || event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            pinching = false;
            if (event.getActionMasked() == MotionEvent.ACTION_UP) performClick();
        }
        return true;
    }

    /**
     * The two useful framing landmarks are where the photo's long or short side
     * exactly touches the square crop boundary.  Keep a separate unsnapped gesture
     * value so a slow pinch can still pull away instead of becoming permanently sticky.
     */
    private float snapZoomIfNear(float rawZoom) {
        long now = SystemClock.uptimeMillis();
        if (now < snappingPausedUntil) return rawZoom;
        float side = Math.max(1f, Math.min(getWidth(), getHeight()));
        float enterRatio = Math.min(.055f,
                SNAP_DISTANCE_DP * getResources().getDisplayMetrics().density / side);
        float releaseRatio = enterRatio * 1.8f;
        float longSideSnap = Math.min(bitmap.getWidth(), bitmap.getHeight())
                / (float) Math.max(bitmap.getWidth(), bitmap.getHeight());
        float shortSideSnap = 1f;

        if (!Float.isNaN(activeSnapZoom)) {
            if (Math.abs(rawZoom / activeSnapZoom - 1f) <= releaseRatio)
                return activeSnapZoom;
            activeSnapZoom = Float.NaN;
        }

        float candidate = nearestValidSnap(rawZoom, longSideSnap, shortSideSnap);
        if (Float.isNaN(candidate)
                || Math.abs(rawZoom / candidate - 1f) > enterRatio)
            return rawZoom;

        if (recordSnapAndShouldPause(now)) {
            snappingPausedUntil = now + SNAP_PAUSE_MS;
            activeSnapZoom = Float.NaN;
            return rawZoom;
        }
        activeSnapZoom = candidate;
        return candidate;
    }

    private float nearestValidSnap(float rawZoom, float first, float second) {
        float min = StringArtGenerator.minimumCropZoom(bitmap);
        float best = Float.NaN;
        float bestDistance = Float.MAX_VALUE;
        float[] candidates = {first, second};
        for (float candidate : candidates) {
            if (candidate < min || candidate > StringArtGenerator.MAX_CROP_ZOOM) continue;
            float distance = Math.abs(rawZoom / candidate - 1f);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    private boolean recordSnapAndShouldPause(long now) {
        int write = 0;
        for (int i = 0; i < recentSnapCount; i++) {
            if (now - recentSnapTimes[i] <= SNAP_REPEAT_WINDOW_MS)
                recentSnapTimes[write++] = recentSnapTimes[i];
        }
        recentSnapCount = write;
        if (recentSnapCount < recentSnapTimes.length)
            recentSnapTimes[recentSnapCount++] = now;
        return recentSnapCount >= recentSnapTimes.length;
    }

    @Override public boolean performClick() { super.performClick(); return true; }
    private float distance(MotionEvent e) { float x=e.getX(0)-e.getX(1), y=e.getY(0)-e.getY(1); return (float)Math.sqrt(x*x+y*y); }
}
