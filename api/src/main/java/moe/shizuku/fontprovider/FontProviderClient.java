package moe.shizuku.fontprovider;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileInputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import moe.shizuku.fontprovider.compat.FontFamilyCompat;
import moe.shizuku.fontprovider.compat.TypefaceCompat;
import moe.shizuku.fontprovider.font.BundledFontFamily;
import moe.shizuku.fontprovider.font.Font;
import moe.shizuku.fontprovider.font.FontFamily;

import static moe.shizuku.fontprovider.FontProviderClient.FontProviderAvailability.DISABLED;
import static moe.shizuku.fontprovider.FontProviderClient.FontProviderAvailability.NOT_INSTALLED;
import static moe.shizuku.fontprovider.FontProviderClient.FontProviderAvailability.OK;
import static moe.shizuku.fontprovider.FontProviderClient.FontProviderAvailability.VERSION_TOO_LOW;

/**
 * Created by rikka on 2017/9/27.
 */

public class FontProviderClient {

    private static final String TAG = "FontProviderClient";

    private static final String PACKAGE = "moe.shizuku.fontprovider";
    private static final int MIN_VERSION = 91;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({OK, NOT_INSTALLED, DISABLED, VERSION_TOO_LOW})
    public @interface FontProviderAvailability {
        /** ok **/
        int OK = 0;

        /** provider app is not installed **/
        int NOT_INSTALLED = 1;

        /** provider app is disabled **/
        int DISABLED = 2;

        /** installed provider app version is too low **/
        int VERSION_TOO_LOW = 3;
    }

    /**
     * Check if Font Provider app is available.
     *
     * @param context Context
     * @return code
     *
     * @see FontProviderAvailability#OK
     * @see FontProviderAvailability#NOT_INSTALLED
     * @see FontProviderAvailability#DISABLED
     * @see FontProviderAvailability#VERSION_TOO_LOW
     */
    public static @FontProviderAvailability int checkAvailability(Context context) {
        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo(PACKAGE, 0);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        if (pi == null) {
            return NOT_INSTALLED;
        }

        if (!pi.applicationInfo.enabled) {
            return DISABLED;
        }

        if (pi.versionCode < MIN_VERSION) {
            return VERSION_TOO_LOW;
        }

        return OK;
    }

    /**
     * Create FontProviderClient.
     *
     * @param context Context
     */
    @Nullable
    public static FontProviderClient create(Context context) {
        if (checkAvailability(context) != OK) {
            return null;
        }

        context = context.getApplicationContext();

        sBufferCache.clear();

        return new FontProviderClient(context);
    }

    private static Map<String, ByteBuffer> sBufferCache = new HashMap<>();

    private final ContentResolver mResolver;
    private final IFontProvider mFontProvider;

    private FontProviderClient(Context context) {
        mResolver = context.getContentResolver();
        mFontProvider = null;
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

    /**
     * Replace font families with specified font, weight will be resolved by family names.
     *
     * @param fontName font name, such as "Noto Sans CJK"
     * @param name font families to replace, such as "sans-serif", "sans-serif-medium"
     * @return Typefaces created from request
     */
    public Typeface[] replace(String fontName, String... name) {
        return replace(FontRequests.DEFAULT_SANS_SERIF_FONTS, fontName, name);
    }

    /**
     * Replace font families with specified font, weight will be resolved by family names.

     * @param defaultFonts default fonts, such as "FontRequests.DEFAULT_SANS_SERIF_FONTS",
     *                     or other FontRequest array
     * @param fontName font name, such as "Noto Sans CJK"
     * @param name font families to replace, such as "sans-serif", "sans-serif-medium"
     * @return Typefaces created from request
     * @return
     */
    public Typeface[] replace(FontRequest[] defaultFonts, String fontName, String... name) {
        int[] weight = new int[name.length];
        for (int i = 0; i < name.length; i++) {
            weight[i] = resolveWeight(name[i]);
        }
        return replace(defaultFonts, fontName, name, weight);
    }

    /**
     * Replace font families with specified font.

     * @param defaultFonts default fonts, such as "FontRequests.DEFAULT_SANS_SERIF_FONTS",
     *                     or other FontRequest array
     * @param fontName font name, such as "Noto Sans CJK"
     * @param name font families to replace, such as "sans-serif", "sans-serif-medium"
     * @param weight font weights
     * @return Typefaces created from request
     */
    public Typeface[] replace(FontRequest[] defaultFonts, String fontName, String[] name, int[] weight) {
        if (name.length != weight.length) {
            throw new IllegalArgumentException("length of name and weight should be same");
        }

        Map<String, Typeface> systemFontMap = TypefaceCompat.getSystemFontMap();
        if (systemFontMap == null) {
            return null;
        }

        Typeface[] typefaces = new Typeface[name.length];
        Typeface base = request(defaultFonts, fontName, weight);
        for (int i = 0; i < name.length; i++) {
            typefaces[i] = weight[i] != 400 ?
                    TypefaceCompat.createWeightAlias(base, weight[i]) : base;
            systemFontMap.put(name[i], typefaces[i]);
        }
        return typefaces;
    }

    /**
     * Request Typeface from default font, name, weights.
     * <p>
     * If you want to use font which weight is not 400, use {@link TypefaceCompat#createWeightAlias(Typeface, int)}
     * to create Typeface with weight.
     *
     * @param defaultFonts default fonts, such as "FontRequests.DEFAULT_SANS_SERIF_FONTS",
     *                     or other FontRequest array
     * @param fontName font name, such as "Noto Sans CJK"
     * @param weight font weight, such as "400, 500"
     * @return Typefaces created from request
     */
    public Typeface request(FontRequest[] defaultFonts, String fontName, int... weight) {
        return request(FontRequests.create(defaultFonts, fontName, weight));
    }

    /**
     * Request Typeface from {@link FontRequests}.
     * <p>
     * If you want to use font which weight is not 400, use {@link TypefaceCompat#createWeightAlias(Typeface, int)}
     * to create Typeface with weight.
     *
     * @param fontRequests FontRequests
     * @return Typefaces created from request
     */
    public Typeface request(FontRequests fontRequests) {
        long time = System.currentTimeMillis();

        BundledFontFamily bundledFontFamily;
        try {
            bundledFontFamily = (mFontProvider != null) ?
                    fontRequests.request(mFontProvider) : fontRequests.request(mResolver);
        } catch (Exception e) {
            return null;
        }

        Log.d(TAG, "get info and load font costs " + (System.currentTimeMillis() - time) + "ms");

        if (bundledFontFamily == null) {
            return null;
        }

        boolean ignoreDefault = fontRequests.ignoreDefault();
        FontFamily[] fontFamilies = bundledFontFamily.families;

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
                        ByteBuffer byteBuffer = sBufferCache.get(font.filename);

                        if (byteBuffer == null) {
                            ParcelFileDescriptor pfd = bundledFontFamily.fd.get(font.filename);
                            int size = (int) font.size;

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
                        String path = font.path;

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

        return TypefaceCompat.createFromFamiliesWithDefault(families, -1, -1);
    }
}
