package moe.shizuku.fontprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;

import moe.shizuku.fontprovider.font.Font;
import moe.shizuku.fontprovider.font.FontFamily;
import moe.shizuku.fontprovider.utils.ContextUtils;

/**
 * Created by rikka on 2017/10/9.
 */

public class FontProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return false;
    }

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
        for (FontFamily family : families) {
            for (Font font : family.fonts) {
                File file =  ContextUtils.getFile(getContext(), font.filename);
                if (file.exists()) {
                    font.path = file.getAbsolutePath();
                    font.size = file.length();
                }
            }
        }

        Parcel parcel = Parcel.obtain();
        parcel.writeTypedArray(families, 0);
        byte[] data = parcel.marshall();
        parcel.recycle();

        MatrixCursor cursor = new MatrixCursor(new String[]{"data"}, 1);
        cursor.addRow(new Object[]{data});
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
}
