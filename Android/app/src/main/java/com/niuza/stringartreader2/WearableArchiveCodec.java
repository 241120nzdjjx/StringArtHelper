/* SPDX-License-Identifier: GPL-3.0-only */
package com.niuza.stringartreader2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/** Compact, deterministic on-band archive encoding. */
final class WearableArchiveCodec {
    private WearableArchiveCodec() { }

    static int bitsForNails(int nails) {
        return WearableBitPacking.bitsForNails(nails);
    }

    static byte[] pack(ArrayList<Integer> values, int nails) {
        return WearableBitPacking.pack(values, nails);
    }

    static long crc32(byte[] data) {
        return WearableBitPacking.crc32(data);
    }

    /** Compact progress preview matching the phone preview's completed/current-line semantics. */
    static byte[] progressPreview(ArrayList<Integer> values, int nails, int index,
                                  int circleMm, float lineMm) {
        if (values == null || values.size() < 2 || nails < 2) return new byte[0];
        final int size = 240;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.rgb(247, 246, 250));
        float cx = size / 2f;
        float cy = size / 2f;
        float radius = size / 2f - 24f;
        Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
        rim.setStyle(Paint.Style.STROKE);
        rim.setColor(Color.rgb(112, 112, 126));
        rim.setStrokeWidth(1f);
        canvas.drawCircle(cx, cy, radius, rim);

        float width = Math.max(.22f, (2f * radius) * Math.max(.01f, lineMm)
                / Math.max(1, circleMm));
        Paint completed = new Paint(Paint.ANTI_ALIAS_FLAG);
        completed.setStyle(Paint.Style.STROKE);
        completed.setStrokeWidth(width);
        completed.setColor(Color.argb(58, 0, 0, 0));
        Paint current = new Paint(Paint.ANTI_ALIAS_FLAG);
        current.setStyle(Paint.Style.STROKE);
        current.setStrokeWidth(Math.max(1f, width * 1.5f));
        current.setColor(Color.rgb(151, 105, 255));

        int safeIndex = Math.max(0, Math.min(index, values.size() - 1));
        for (int i = 1; i < safeIndex; i++)
            drawSegment(canvas, values.get(i - 1), values.get(i), nails,
                    cx, cy, radius, completed);
        if (safeIndex > 0)
            drawSegment(canvas, values.get(safeIndex - 1), values.get(safeIndex), nails,
                    cx, cy, radius, current);

        Paint nail = new Paint(Paint.ANTI_ALIAS_FLAG);
        nail.setColor(Color.rgb(104, 104, 116));
        Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
        label.setColor(Color.rgb(70, 70, 82));
        NailIndexRenderer.draw(canvas, nails, cx, cy, radius, 1f, nail, label);

        int active = values.get(safeIndex);
        double angle = Math.PI * 2d * active / nails;
        Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
        dot.setStyle(Paint.Style.FILL);
        dot.setColor(Color.rgb(151, 105, 255));
        canvas.drawCircle((float) (cx + Math.cos(angle) * radius),
                (float) (cy + Math.sin(angle) * radius), 4f, dot);

        ByteArrayOutputStream out = new ByteArrayOutputStream(16 * 1024);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 42, out);
        bitmap.recycle();
        return out.toByteArray();
    }

    private static void drawSegment(Canvas canvas, int from, int to, int nails,
                                    float cx, float cy, float radius, Paint paint) {
        if (from < 0 || to < 0 || from >= nails || to >= nails) return;
        double fromAngle = Math.PI * 2d * from / nails;
        double toAngle = Math.PI * 2d * to / nails;
        canvas.drawLine((float) (cx + Math.cos(fromAngle) * radius),
                (float) (cy + Math.sin(fromAngle) * radius),
                (float) (cx + Math.cos(toAngle) * radius),
                (float) (cy + Math.sin(toAngle) * radius), paint);
    }

    /** 64x64 grayscale JPEG at deliberately aggressive quality for the band list. */
    static byte[] tinyThumbnail(byte[] source) {
        if (source == null || source.length == 0) return new byte[0];
        Bitmap decoded = BitmapFactory.decodeByteArray(source, 0, source.length);
        if (decoded == null) return new byte[0];
        Bitmap tiny = Bitmap.createBitmap(64, 64, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(tiny);
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        float scale = Math.min(64f / decoded.getWidth(), 64f / decoded.getHeight());
        float left = (64f - decoded.getWidth() * scale) / 2f;
        float top = (64f - decoded.getHeight() * scale) / 2f;
        canvas.drawBitmap(decoded, null,
                new android.graphics.RectF(left, top, left + decoded.getWidth() * scale,
                        top + decoded.getHeight() * scale), paint);
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        tiny.compress(Bitmap.CompressFormat.JPEG, 18, out);
        decoded.recycle();
        tiny.recycle();
        return out.toByteArray();
    }
}
