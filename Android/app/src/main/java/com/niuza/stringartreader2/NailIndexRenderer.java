/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 */
package com.niuza.stringartreader2;

import android.graphics.Canvas;
import android.graphics.Paint;

/** Draws nail dots and radially oriented indices outside a circular nail ring. */
final class NailIndexRenderer {
    private static final int MAX_SAFE_LABELS = 5000;

    private NailIndexRenderer() { }

    static void draw(Canvas canvas, int nails, float cx, float cy, float radius,
                     float density, Paint dotPaint, Paint labelPaint) {
        if (nails < 2 || radius <= 0f) return;
        float arcPx = (float) (Math.PI * 2d * radius / nails);
        float baseTextPx = Math.max(3f, Math.min(6f * density, arcPx * .72f));
        float dotRadius = Math.max(.8f, Math.min(2.2f * density, arcPx * .14f));
        float labelRadius = radius + dotRadius + Math.max(2f, baseTextPx * .62f);
        int stride = Math.max(1, (int) Math.ceil(nails / (double) MAX_SAFE_LABELS));

        dotPaint.setStyle(Paint.Style.FILL);
        labelPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < nails; i += stride) {
            double angle = Math.PI * 2d * i / nails;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            canvas.drawCircle(cx + cos * radius, cy + sin * radius, dotRadius, dotPaint);

            float scale = i % 10 == 0 ? 2f : (i % 5 == 0 ? 1.5f : 1f);
            labelPaint.setTextSize(baseTextPx * scale);
            canvas.save();
            canvas.translate(cx + cos * labelRadius, cy + sin * labelRadius);
            float degrees = (float) Math.toDegrees(angle);
            if (cos < 0f) {
                canvas.rotate(degrees + 180f);
                labelPaint.setTextAlign(Paint.Align.RIGHT);
            } else {
                canvas.rotate(degrees);
                labelPaint.setTextAlign(Paint.Align.LEFT);
            }
            Paint.FontMetrics metrics = labelPaint.getFontMetrics();
            float baseline = -(metrics.ascent + metrics.descent) * .5f;
            canvas.drawText(String.valueOf(i), 0f, baseline, labelPaint);
            canvas.restore();
        }
    }
}
