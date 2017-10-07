package moe.shizuku.fontprovider;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
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
        boolean onServiceConnected(FontProviderClient client);
    }

    /**
     * Create service connection.
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
            context.bindService(intent, new FontProviderServiceConnection(context, callback), Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.i(TAG, "can't bindService", e);
        }
    }

    private static Map<String, ByteBuffer> sBufferCache = new HashMap<>();

    private IFontProvider mFontProvider;
    private ServiceConnection mServiceConnection;

    public FontProviderClient(ServiceConnection serviceConnection, IFontProvider fontProvider) {
        mServiceConnection = serviceConnection;
        mFontProvider = fontProvider;
    }

    /**
     * Unbind service, only need call this if false is returned in onServiceConnected
     *
     * @param context Context
     */
    public void unbindService(Context context) {
        try {
            context.getApplicationContext().unbindService(mServiceConnection);
        } catch (Exception e) {
            Log.i(TAG, "can't unbindService", e);
        }
    }

    /**=*
     * @return IFontProvider
     */
    public IFontProvider getFontProvider() {
        return mFontProvider;
    }

    private static int resolveWeight(String name) {
        if (TextUtils.isEmpty(name)) {
            return 400;
        }

        if (name.endsWith("-thin")) {
            return 100;
        } else if (name.endsWith("-demilight")) {
            return 200;
        } else if (name.endsWith("-light")) {
            return 300;
        } else if (name.endsWith("-medium")) {
            return 500;
        } else if (name.endsWith("-bold")) {
            return 700;
        } else if (name.endsWith("-black")) {
            return 900;
        } else {
            return 400;
        }
    }

    private static boolean resolveIsSerif(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        return name.startsWith("serif");
    }

    /**
     * Replace font family with specified font, weight will be resolved by family name.
     *
     * @param name font family name
     * @param fontName font name, such as "Noto Sans CJK"
     * @return Typeface using to replace.
     */
    public Typeface replace(String name, String fontName) {
        return replace(name, fontName, resolveIsSerif(name) ? FontRequest.NOTO_SERIF : FontRequest.DEFAULT);
    }

    /**
     * Replace font family with specified font.
     *
     * @param name font family name
     * @param fontName font name, such as "Noto Sans CJK"
     * @param defaultFont first font
     * @return Typeface using to replace.
     */
    public Typeface replace(String name, String fontName, FontRequest defaultFont) {
        return replace(name, FontRequests.create(resolveWeight(name), defaultFont, fontName));
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
                fontFamilies = FontFamily.combine(fontFamilies, fontRequest.getFontFamily(mFontProvider));
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
                            ParcelFileDescriptor pfd = mFontProvider.getFontFileDescriptor(font.filename);
                            if (pfd == null) {
                                Log.e(TAG, "ParcelFileDescriptor is null");
                                return null;
                            }
                            int size = mFontProvider.getFontFileSize(font.filename);

                            FileInputStream is = new FileInputStream(pfd.getFileDescriptor());
                            FileChannel fileChannel = is.getChannel();
                            byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
                        }

                        if (!fontFamilyCompat.addFont(byteBuffer, font.ttcIndex, font.weight, font.italic ? 1 : 0)) {
                            return null;
                        }
                    } else {
                        String path = mFontProvider.getFontFilePath(font.filename);
                        if (path == null) {
                            Log.e(TAG, "Font not downloaded?");
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

        return TypefaceCompat.createFromFamiliesWithDefault(families, fontRequests.weight, -1);
    }
}
