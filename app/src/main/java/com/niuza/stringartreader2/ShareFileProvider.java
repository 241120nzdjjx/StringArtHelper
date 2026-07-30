/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 */
package com.niuza.stringartreader2;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/** Read-only, temporary access to files intentionally copied into the app's share cache. */
public final class ShareFileProvider extends ContentProvider {
    private static final String PATH = "shared";

    static Uri uriFor(Context context, File file) {
        return new Uri.Builder()
                .scheme("content")
                .authority(context.getPackageName() + ".share")
                .appendPath(PATH)
                .appendPath(file.getName())
                .build();
    }

    @Override public boolean onCreate() { return true; }

    @Override
    public String getType(Uri uri) {
        String name = uri.getLastPathSegment();
        if (name == null) return "application/octet-stream";
        String lower = name.toLowerCase();
        if (lower.endsWith(".apk")) return "application/vnd.android.package-archive";
        return "application/octet-stream";
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (!"r".equals(mode)) throw new FileNotFoundException("Read-only provider");
        return ParcelFileDescriptor.open(resolve(uri), ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        File file;
        try { file = resolve(uri); }
        catch (FileNotFoundException e) { return null; }
        String[] columns = projection == null
                ? new String[] {OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE}
                : projection;
        MatrixCursor cursor = new MatrixCursor(columns, 1);
        MatrixCursor.RowBuilder row = cursor.newRow();
        String displayName = file.getName();
        int separator = displayName.indexOf("__");
        if (separator >= 0 && separator + 2 < displayName.length())
            displayName = displayName.substring(separator + 2);
        for (String column : columns) {
            if (OpenableColumns.DISPLAY_NAME.equals(column)) row.add(displayName);
            else if (OpenableColumns.SIZE.equals(column)) row.add(file.length());
            else row.add(null);
        }
        return cursor;
    }

    private File resolve(Uri uri) throws FileNotFoundException {
        Context context = getContext();
        if (context == null || uri.getPathSegments().size() != 2
                || !PATH.equals(uri.getPathSegments().get(0)))
            throw new FileNotFoundException("Invalid share URI");
        File root = new File(context.getCacheDir(), "shared_files");
        File candidate = new File(root, uri.getPathSegments().get(1));
        try {
            String rootPath = root.getCanonicalPath() + File.separator;
            if (!candidate.getCanonicalPath().startsWith(rootPath) || !candidate.isFile())
                throw new FileNotFoundException("Shared file not found");
        } catch (IOException e) {
            throw new FileNotFoundException("Invalid share path");
        }
        return candidate;
    }

    @Override public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Read-only provider");
    }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Read-only provider");
    }
    @Override public int update(Uri uri, ContentValues values, String selection,
                                String[] selectionArgs) {
        throw new UnsupportedOperationException("Read-only provider");
    }
}
