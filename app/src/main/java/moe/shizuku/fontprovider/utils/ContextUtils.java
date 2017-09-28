package moe.shizuku.fontprovider.utils;

import android.content.Context;

import java.io.File;

/**
 * Created by rikka on 2017/9/27.
 */

public class ContextUtils {

    public static File getCacheFile(Context context, String name) {
        if (context.getExternalCacheDir() == null) {
            return new File(context.getCacheDir(), name);
        } else {
            return new File(context.getExternalCacheDir(), name);
        }
    }

    public static File getFile(Context context, String name) {
        if (context.getExternalFilesDir(null) == null) {
            return new File(context.getFilesDir(), name);
        } else {
            return new File(context.getExternalFilesDir(null), name);
        }
    }
}
