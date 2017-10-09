package moe.shizuku.fontprovider.font;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;

import moe.shizuku.fontprovider.utils.ContextUtils;

/**
 * Created by rikka on 2017/10/9.
 */

public class FontProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return false;
    }

    @SuppressLint("NewApi")
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        String path = uri.getEncodedPath();
        if (!path.startsWith("/font/")
                || selectionArgs == null ||  selectionArgs[0] == null) {
            return null;
        }

        String name = path.substring("/font/".length());
        String[] w = selectionArgs[0].split(",");
        int[] weight = new int[w.length];
        for (int i = 0; i < w.length; i++) {
            weight[i] = Integer.parseInt(w[i]);
        }

        FontFamily[] families = FontManager.getFontFamily(name, weight);
        if (families != null) {
            for (FontFamily family : families) {
                for (Font font : family.fonts) {
                    File file =  ContextUtils.getFile(getContext(), font.filename);
                    if (file.exists()) {
                        font.path = file.getAbsolutePath();
                        font.size = file.length();
                    }
                }
            }
        }

        Bundle bundle = new Bundle();
        bundle.putParcelableArray("data", families);

        Cursor cursor = new SimpleCursor();
        cursor.setExtras(bundle);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        String path = uri.getEncodedPath();
        if (!path.startsWith("/file/")) {
            return null;
        }

        return FontManager.getParcelFileDescriptor(getContext(), path.substring("/file/".length()));
    }

    @Nullable
    @Override
    public AssetFileDescriptor openAssetFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        return new AssetFileDescriptor(openFile(uri, mode), 0, 0);
    }
}
