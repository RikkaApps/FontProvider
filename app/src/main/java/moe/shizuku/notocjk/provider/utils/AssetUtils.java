package moe.shizuku.notocjk.provider.utils;

import android.content.Context;
import android.os.MemoryFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by rikka on 2017/9/27.
 */

public class AssetUtils {

    public static File toCacheFile(Context context, String fileName) {
        File file = ContextUtils.getCacheFile(context, fileName);
        if (file.exists()) {
            return file;
        }

        try {
            InputStream is = context.getAssets().open(fileName);
            OutputStream os = new FileOutputStream(file);
            byte[] b = new byte[8192];
            for (int r; (r = is.read(b)) != -1;) {
                os.write(b, 0, r);
            }
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MemoryFile toMemoryFile(Context context, String fileName, int length) {
        try {
            MemoryFile file = new MemoryFile(fileName, length);

            InputStream is = context.getAssets().open(fileName);
            OutputStream os = file.getOutputStream();
            byte[] b = new byte[8192];
            for (int r; (r = is.read(b)) != -1;) {
                os.write(b, 0, r);
            }
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
