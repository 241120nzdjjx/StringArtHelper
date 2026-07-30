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
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Incremental, bitmap-backed visualization used while a generated sequence is
 * being revealed. Only newly revealed chords are painted, so long 20,000-line
 * projects do not redraw every previous chord on every animation frame.
 */
@android.annotation.SuppressLint("ViewConstructor")
final class GenerationProgressView extends View {
    private final Paint threadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ArrayList<Integer> revealed = new ArrayList<Integer>();
    private Bitmap buffer;
    private Canvas bufferCanvas;
    private int pinCount;
    private float circleMm;
    private float threadMm;

    GenerationProgressView(Context context, int nails, int diameterMm, float lineMm) {
        super(context);
        pinCount = Math.max(2, nails);
        circleMm = Math.max(1f, diameterMm);
        threadMm = Math.max(.01f, Math.min(1f, lineMm));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.5f);
        borderPaint.setColor(0xFF333333);
    }

    void appendUntil(List<Integer> sequence, int count) {
        int target = Math.max(0, Math.min(count, sequence == null ? 0 : sequence.size()));
        if (target <= revealed.size()) return;
        int oldSize = revealed.size();
        for (int i = oldSize; i < target; i++) revealed.add(sequence.get(i));
        if (bufferCanvas != null) {
            int firstLine = Math.max(1, oldSize);
            for (int i = firstLine; i < revealed.size(); i++) drawChord(bufferCanvas,
                    revealed.get(i - 1), revealed.get(i));
        }
        invalidate();
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;
        if (buffer != null) buffer.recycle();
        buffer = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bufferCanvas = new Canvas(buffer);
        rebuild();
    }

    private void rebuild() {
        if (bufferCanvas == null) return;
        bufferCanvas.drawColor(Color.rgb(248, 247, 251));
        configureThreadPaint();
        for (int i = 1; i < revealed.size(); i++)
            drawChord(bufferCanvas, revealed.get(i - 1), revealed.get(i));
        float side = Math.min(getWidth(), getHeight());
        bufferCanvas.drawCircle(getWidth() * .5f, getHeight() * .5f, side * .45f, borderPaint);
    }

    private void configureThreadPaint() {
        float side = Math.min(getWidth(), getHeight());
        float ratio = threadMm / circleMm;
        threadPaint.setStyle(Paint.Style.STROKE);
        threadPaint.setStrokeWidth(Math.max(.12f, side * .9f * ratio));
        int alpha = Math.max(26, Math.min(82, Math.round(26f + side * ratio * 90f)));
        threadPaint.setColor(Color.argb(alpha, 18, 18, 18));
    }

    private void drawChord(Canvas canvas, int a, int b) {
        if (a < 0 || a >= pinCount || b < 0 || b >= pinCount) return;
        configureThreadPaint();
        float side = Math.min(getWidth(), getHeight());
        float cx = getWidth() * .5f, cy = getHeight() * .5f, radius = side * .45f;
        float ax = cx + (float) Math.cos(Math.PI * 2d * a / pinCount) * radius;
        float ay = cy + (float) Math.sin(Math.PI * 2d * a / pinCount) * radius;
        float bx = cx + (float) Math.cos(Math.PI * 2d * b / pinCount) * radius;
        float by = cy + (float) Math.sin(Math.PI * 2d * b / pinCount) * radius;
        canvas.drawLine(ax, ay, bx, by, threadPaint);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (buffer != null) canvas.drawBitmap(buffer, 0f, 0f, null);
        else canvas.drawColor(Color.rgb(248, 247, 251));
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (buffer != null) {
            buffer.recycle();
            buffer = null;
            bufferCanvas = null;
        }
    }
}
