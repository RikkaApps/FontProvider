package moe.shizuku.fontprovider.font;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.util.Log;
import android.util.LruCache;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileDescriptor;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moe.shizuku.fontprovider.BuildConfig;
import moe.shizuku.fontprovider.R;
import moe.shizuku.fontprovider.utils.ContextUtils;
import moe.shizuku.fontprovider.utils.MemoryFileUtils;
import moe.shizuku.fontprovider.utils.ParcelFileDescriptorUtils;

import static moe.shizuku.fontprovider.BuildConfig.BUILT_IN_FONTS_SIZE;

/**
 * Created by rikka on 2017/9/29.
 */

public class FontManager {

    private static final String TAG = "FontManager";

    private static final LruCache<String, MemoryFile> FILE_CACHE;
    private static final Map<String, Integer> FILE_SIZE = BUILT_IN_FONTS_SIZE;

    private static final int MAX_CACHE = 1024 * 1024 * 100;

    static {
        FILE_CACHE = new LruCache<String, MemoryFile>(MAX_CACHE) {

            @Override
            protected void entryRemoved(boolean evicted, String key, MemoryFile oldValue, MemoryFile newValue) {
                if (evicted) {
                    oldValue.close();
                }
            }

            @Override
            protected int sizeOf(String key, MemoryFile value) {
                return value.length();
            }
        };
    }

    private static final @RawRes int[] FONTS_RES = {
            R.raw.noto_sans_cjk,
            R.raw.noto_serif,
            R.raw.noto_serif_cjk,
            R.raw.noto_color_emoji,
    };

    private static final Map<String, FontInfo> FONTS_MAP = new HashMap<>();
    private static final List<FontInfo> FONTS = new ArrayList<>();

    public static void init(Context context) {
        if (FONTS_MAP.size() > 0) {
            return;
        }

        for (int res : FONTS_RES) {
            FontInfo font = new Gson().fromJson(new InputStreamReader(context.getResources().openRawResource(res)), FontInfo.class);
            FONTS_MAP.put(font.getName(), font);
            FONTS.add(font);
        }
    }

    public static List<FontInfo> getFonts() {
        return FONTS;
    }

    public static FontInfo getFont(String name) {
        return FONTS_MAP.get(name);
    }

    public static List<String> getFiles(FontInfo font, Context context, boolean excludeExisted) {
        List<String> files = new ArrayList<>();
        for (FontInfo.Style style : font.getStyle()) {
            if (style.getTtc() != null) {
                files.add(style.getTtc());
            } else {
                files.addAll(Arrays.asList(style.getTtf()));
            }
        }

        if (excludeExisted) {
            for (String f : new ArrayList<>(files)) {
                if (ContextUtils.getFile(context, f).exists()) {
                    files.remove(f);
                }
            }
        }
        return files;
    }

    public static List<Long> download(FontInfo font, List<String> files, Context context) {
        List<Long> ids = new ArrayList<>();

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        // TODO already downloading?
        for (String f : files) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(font.getUrlPrefix() + f))
                    .setDestinationInExternalFilesDir(context, null, f)
                    .setVisibleInDownloadsUi(false)
                    .setAllowedOverMetered(false || BuildConfig.DEBUG)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            ids.add(downloadManager.enqueue(request));
        }

        return ids;
    }

    public static FontFamily[] getFontFamily(Context context, String name, @Nullable int... weight) {
        FontInfo font = FontManager.getFont(name);
        if (font == null) {
            return null;
        }

        String[] language = font.getLanguage();
        int[] index = font.getIndex();

        // each language will have FontFamily
        FontFamily[] families = new FontFamily[language.length];

        for (int i = 0; i < families.length; i++) {
            FontInfo.Style[] styles = font.getStyle(weight);

            // each style have a Font
            Font[] fonts = new Font[styles.length];

            for (int j = 0; j < styles.length; j++) {
                FontInfo.Style style = styles[j];
                if (style.getTtc() != null) {
                    fonts[j] = new Font(style.getTtc(), index[i], style.getWeight(), style.isItalic());
                } else {
                    String[] ttf = style.getTtf();
                    for (int k : index) {
                        fonts[j] = new Font(ttf[k], 0, style.getWeight(), style.isItalic());
                    }
                }
            }

            families[i] = new FontFamily(language[i], font.getVariant(), fonts);
        }

        // fill file info
        for (FontFamily family : families) {
            for (Font f : family.fonts) {
                File file =  ContextUtils.getFile(context, f.filename);
                if (file.exists()) {
                    f.path = file.getAbsolutePath();
                    f.size = file.length();
                }
            }
        }

        return families;
    }

    public static ParcelFileDescriptor getParcelFileDescriptor(Context context, String filename) {
        FileDescriptor fd = getFileDescriptor(context, filename);
        if (fd != null) {
            return ParcelFileDescriptorUtils.dupSilently(fd);
        } else {
            return null;
        }
    }

    public static FileDescriptor getFileDescriptor(Context context, String filename) {
        MemoryFile mf = FILE_CACHE.get(filename);

        if (mf != null) {
            Log.i(TAG, "MemoryFile " + filename + " is in the cache");

            FileDescriptor fd = MemoryFileUtils.getFileDescriptor(mf);
            if (fd != null && fd.valid()) {
                return fd;
            } else {
                Log.i(TAG, "MemoryFile " + filename + " is not valid?");
            }
        }

        long time = System.currentTimeMillis();

        Log.i(TAG, "loading file " + filename);

        // built in font? read from asset
        if (BUILT_IN_FONTS_SIZE.containsKey(filename)) {
            mf = MemoryFileUtils.fromAsset(context.getAssets(), filename, FILE_SIZE.get(filename));
        }

        // downloadable font? read from file
        if (mf == null) {
            File file = ContextUtils.getFile(context, filename);
            if (file.exists()) {
                mf = MemoryFileUtils.fromFile(file);
                if (mf != null) {
                    FILE_SIZE.put(filename, mf.length());
                }
            }
        }

        // file not exist?
        if (mf == null) {
            Log.w(TAG, "loading " + filename + " failed");
            return null;
        }

        Log.i(TAG, "loading finished in " + (System.currentTimeMillis() - time) + "ms");
        FILE_CACHE.put(filename, mf);

        return MemoryFileUtils.getFileDescriptor(mf);
    }

    public static int getFileSize(Context context, String filename) {
        if (FILE_SIZE.containsKey(filename)) {
            return FILE_SIZE.get(filename);
        }

        File file = ContextUtils.getFile(context, filename);
        if (file.exists()) {
            return (int) file.length();
        }
        return 0;
    }

    public static void closeAll() {
        FILE_CACHE.trimToSize(0);
    }
}
