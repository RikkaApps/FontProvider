package moe.shizuku.fontprovider.font;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.os.SharedMemory;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.annotation.RequiresApi;
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

import moe.shizuku.fontprovider.FontProviderSettings;
import moe.shizuku.fontprovider.FontRequest;
import moe.shizuku.fontprovider.FontRequests;
import moe.shizuku.fontprovider.R;
import moe.shizuku.fontprovider.utils.MemoryFileUtils;
import moe.shizuku.fontprovider.utils.ParcelFileDescriptorUtils;
import moe.shizuku.fontprovider.utils.SharedMemoryUtils;
import moe.shizuku.support.utils.ContextUtils;

import static moe.shizuku.fontprovider.BuildConfig.BUILT_IN_FONTS_SIZE;

/**
 * Created by rikka on 2017/9/29.
 */

public class FontManager {

    private static final String TAG = "FontManager";

    private static final @RawRes int[] FONTS_RES = {
            R.raw.noto_sans_cjk,
            R.raw.noto_serif,
            R.raw.noto_serif_cjk,
            R.raw.noto_color_emoji,
    };

    private static final Map<String, Integer> FILE_SIZE = new HashMap<>(BUILT_IN_FONTS_SIZE);

    private static LruCache<String, MemoryFile> sCache;
    private static LruCache<String, SharedMemory> sCacheV27;
    private static List<FontInfo> sFonts;

    public static void init(Context context) {
        if (sFonts != null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            sCacheV27 = new LruCache<String, SharedMemory>(FontProviderSettings.getMaxCache()) {
                @Override
                protected void entryRemoved(boolean evicted, String key, SharedMemory oldValue, SharedMemory newValue) {
                    if (evicted) {
                        oldValue.close();
                    }
                }

                @Override
                protected int sizeOf(String key, SharedMemory value) {
                    return value.getSize();
                }
            };
        } else {
            sCache = new LruCache<String, MemoryFile>(FontProviderSettings.getMaxCache()) {
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

        sFonts = new ArrayList<>();

        for (int res : FONTS_RES) {
            FontInfo font = new Gson().fromJson(new InputStreamReader(context.getResources().openRawResource(res)), FontInfo.class);
            sFonts.add(font);
        }
    }

    public static List<FontInfo> getFonts() {
        return sFonts;
    }

    public static FontInfo getFont(String name) {
        for (FontInfo font : sFonts) {
            if (font.getName().equals(name)) {
                return font;
            }
        }
        return null;
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
                if (ContextUtils.getExternalFile(context, f).exists()) {
                    files.remove(f);
                }
            }
        }
        return files;
    }

    public static List<Long> download(FontInfo font, List<String> files, Context context, boolean allowDownloadOverMetered) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager == null) {
            return null;
        }

        Cursor cursor = downloadManager.query(new DownloadManager.Query()
                .setFilterByStatus(DownloadManager.STATUS_PAUSED
                        | DownloadManager.STATUS_PENDING
                        | DownloadManager.STATUS_RUNNING));

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                do {
                    int titleIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE);
                    String file = cursor.getString(titleIndex);
                    Log.d(TAG, file + " is already downloading");
                    files.remove(file);
                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        List<Long> ids = new ArrayList<>();
        for (String f : files) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(font.getUrlPrefix() + f))
                    .setTitle(f)
                    .setDestinationInExternalFilesDir(context, null, f)
                    .setVisibleInDownloadsUi(false)
                    .setAllowedOverMetered(allowDownloadOverMetered)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            ids.add(downloadManager.enqueue(request));
        }

        return ids;
    }

    @Nullable
    public static FontFamily[] getFontFamily(Context context, String name, @Nullable int... weight) {
        FontInfo font = FontManager.getFont(name);
        if (font == null) {
            return null;
        }

        String[] language = font.getLanguage();
        int[] index = font.getTtcIndex();

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
                    fonts[j] = new Font(style.getTtf()[i], 0, style.getWeight(), style.isItalic());
                }
            }

            families[i] = new FontFamily(language[i], font.getVariant(), fonts);
        }

        // fill file info
        for (FontFamily family : families) {
            for (Font f : family.fonts) {
                File file =  ContextUtils.getExternalFile(context, f.filename);
                if (file.exists()) {
                    f.path = file.getAbsolutePath();
                    f.size = file.length();
                }
            }
        }

        return families;
    }

    public static BundledFontFamily getBundledFontFamily(Context context, FontRequests requests) {
        FontFamily[] families = new FontFamily[0];
        Map<String, ParcelFileDescriptor> fd = new HashMap<>();
        Map<String, SharedMemory> sm = new HashMap<>();

        for (FontRequest request : requests.requests) {
            families = FontFamily.combine(families, getFontFamily(context, request.name, request.weight));
        }

        if (Build.VERSION.SDK_INT >= 27) {
            for (FontFamily f : families) {
                for (Font font : f.fonts) {
                    SharedMemory smItem = sm.get(font.filename);
                    if (smItem == null) {
                        smItem = getSharedMemory(context, font.filename);
                        if (smItem != null) {
                            sm.put(font.filename, smItem);
                        }
                    }
                }
            }
        } else if (Build.VERSION.SDK_INT >= 24) {
            for (FontFamily f : families) {
                for (Font font : f.fonts) {
                    ParcelFileDescriptor pfd = fd.get(font.filename);
                    if (pfd == null) {
                        pfd = getParcelFileDescriptor(context, font.filename);
                        if (pfd != null) {
                            fd.put(font.filename, pfd);
                        }
                    }
                }
            }
        }
        return new BundledFontFamily(families, fd, sm);
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
        MemoryFile mf = sCache.get(filename);

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
            File file = ContextUtils.getExternalFile(context, filename);
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
        sCache.put(filename, mf);

        return MemoryFileUtils.getFileDescriptor(mf);
    }

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    public static SharedMemory getSharedMemory(Context context, String filename) {
        SharedMemory sm = sCacheV27.get(filename);

        if (sm == null) {
            long time = System.currentTimeMillis();

            Log.i(TAG, "loading file " + filename);

            File file = ContextUtils.getExternalFile(context, filename);
            if (file.exists()) {
                sm = SharedMemoryUtils.fromFile(file);
                if (sm != null) {
                    FILE_SIZE.put(filename, sm.getSize());
                    Log.i(TAG, "loading finished in " + (System.currentTimeMillis() - time) + "ms");
                    sCacheV27.put(filename, sm);
                }
            }
        }

        return sm;
    }

    public static int getFileSize(Context context, String filename) {
        if (FILE_SIZE.containsKey(filename)) {
            return FILE_SIZE.get(filename);
        }

        File file = ContextUtils.getExternalFile(context, filename);
        if (file.exists()) {
            return (int) file.length();
        }
        return 0;
    }

    public static void closeAll() {
        sCache.trimToSize(0);
    }

    public static void setMaxCache(int maxSize) {
        sCache.resize(maxSize);
    }
}
