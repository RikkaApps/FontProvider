package moe.shizuku.fontprovider;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import moe.shizuku.fontprovider.compat.FontFamilyCompat;
import moe.shizuku.fontprovider.compat.TypefaceCompat;
import moe.shizuku.fontprovider.font.Font;
import moe.shizuku.fontprovider.font.FontFamily;

/**
 * Created by rikka on 2017/9/27.
 */

public class FontProviderClient {

    private static final String TAG = "FontProviderClient";

    private static final String ACTION = "moe.shizuku.fontprovider.action.BIND";
    private static final String PACKAGE = "moe.shizuku.fontprovider";

    public interface Callback {
        /**
         * Called after ServiceConnection#onServiceConnected.
         *
         * @param client FontProviderClient
         * @return true for unbindService automatically, false for keep ServiceConnection.
         */
        boolean onServiceConnected(FontProviderClient client, ServiceConnection serviceConnection);
    }

    /**
     * Create FontProviderClient asynchronously (use bind service, faster).
     *
     * @param context Context
     * @param callback Callback
     */
    public static void create(Context context, Callback callback) {
        context = context.getApplicationContext();

        Intent intent = new Intent(ACTION)
                .setPackage(PACKAGE);

        sBufferCache.clear();

        try {
            FontProviderServiceConnection connection = new FontProviderServiceConnection(context, callback);
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.i(TAG, "can't bindService", e);
        }
    }

    /**
     * Create FontProviderClient synchronously (use ContentProvider, slower).
     *
     * @param context Context
     */
    public static FontProviderClient createSync(Context context) {
        context = context.getApplicationContext();

        sBufferCache.clear();

        return new FontProviderClient(context);
    }

    private static Map<String, ByteBuffer> sBufferCache = new HashMap<>();

    private final boolean mUseContentProvider;

    private ContentResolver mResolver;
    private IFontProvider mFontProvider;

    private FontProviderClient(Context context) {
        mResolver = context.getContentResolver();
        mUseContentProvider = true;
    }

    FontProviderClient(Context context, IFontProvider fontProvider) {
        mResolver = context.getContentResolver();
        mFontProvider = fontProvider;
        mUseContentProvider = false;
    }

    /**
     * Replace font family with specified font, weight will be resolved by family name.
     *
     * @param name font family name
     * @param fontName font name, such as "Noto Sans CJK"
     * @return Typeface using to replace.
     */
    public Typeface replace(String name, String fontName) {
        return replace(name, FontRequests.create(name, fontName));
    }

    public Typeface replace(String name, FontRequests fontRequests) {
        Typeface typeface = request(fontRequests);
        if (typeface != null
                && TypefaceCompat.getSystemFontMap() != null) {
            TypefaceCompat.getSystemFontMap().put(name, typeface);
            return typeface;
        } else {
            return null;
        }
    }

    public Typeface request(FontRequests fontRequests) {
        boolean ignoreDefault = true;
        FontFamily[] fontFamilies = new FontFamily[0];

        for (FontRequest fontRequest : fontRequests.requests) {
            if (fontRequest.equals(FontRequest.DEFAULT)) {
                ignoreDefault = false;
                continue;
            }
            try {
                long time = System.currentTimeMillis();

                if (!mUseContentProvider) {
                    fontFamilies = FontFamily.combine(fontFamilies, fontRequest.loadFontFamily(mFontProvider));
                } else {
                    fontFamilies = FontFamily.combine(fontFamilies, fontRequest.loadFontFamily(mResolver));
                }

                Log.d(TAG, "get info for "+ fontRequest.name + " costs " + (System.currentTimeMillis() - time) + "ms");
            } catch (RemoteException e) {
                e.printStackTrace();
                return null;
            }
        }

        Object families;
        if (!ignoreDefault) {
            families = Array.newInstance(FontFamilyCompat.getFontFamilyClass(), fontFamilies.length + 1);
        } else {
            families = Array.newInstance(FontFamilyCompat.getFontFamilyClass(), fontFamilies.length);
        }

        int i = 0;
        Object fallbackFonts = TypefaceCompat.getFallbackFontsArray();
        if (fallbackFonts == null
                || Array.getLength(fallbackFonts) == 0) {
            return null;
        }

        if (!ignoreDefault) {
            Array.set(families, i++, Array.get(fallbackFonts, 0));
        }

        for (FontFamily fontFamily : fontFamilies) {
            FontFamilyCompat fontFamilyCompat = new FontFamilyCompat(fontFamily.language, fontFamily.variant);
            if (fontFamilyCompat.getFontFamily() == null) {
                return null;
            }

            for (Font font : fontFamily.fonts) {
                try {
                    if (Build.VERSION.SDK_INT >= 24) {
                        ByteBuffer byteBuffer = font.buffer != null ?
                                font.buffer : sBufferCache.get(font.filename);

                        if (byteBuffer == null) {
                            long time = System.currentTimeMillis();

                            ParcelFileDescriptor pfd;
                            int size;
                            if (!mUseContentProvider) {
                                pfd = mFontProvider.getFontFileDescriptor(font.filename);
                                size = mFontProvider.getFontFileSize(font.filename);
                            } else {
                                pfd = mResolver.openAssetFileDescriptor(
                                        Uri.parse("content://moe.shizuku.fontprovider/file/" + font.filename), "r").getParcelFileDescriptor();
                                size = (int) font.size;
                            }

                            Log.d(TAG, "open file " + font.filename + " costs " + (System.currentTimeMillis() - time) + "ms");

                            if (pfd == null) {
                                Log.e(TAG, "ParcelFileDescriptor is null");
                                return null;
                            }

                            FileInputStream is = new FileInputStream(pfd.getFileDescriptor());
                            FileChannel fileChannel = is.getChannel();
                            byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);

                            sBufferCache.put(font.filename, byteBuffer);
                        }

                        if (!fontFamilyCompat.addFont(byteBuffer, font.ttcIndex, font.weight, font.italic ? 1 : 0)) {
                            return null;
                        }
                    } else {
                        String path;
                        if (!mUseContentProvider) {
                            path = mFontProvider.getFontFilePath(font.filename);
                        } else {
                            path = font.path;
                        }

                        if (path == null) {
                            Log.e(TAG, "Font " + font.filename + " not downloaded?");
                            return null;
                        }

                        if (!fontFamilyCompat.addFont(path, font.weight, font.italic ? 1 : 0)) {
                            return null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            if (!fontFamilyCompat.freeze()) {
                return null;
            }

            Array.set(families, i++, fontFamilyCompat.getFontFamily());
        }

        return TypefaceCompat.createFromFamiliesWithDefault(families, fontRequests.weight[0], -1);
    }
}
