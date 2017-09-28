package moe.shizuku.fontprovider.api;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import moe.shizuku.fontprovider.IFontProvider;
import moe.shizuku.fontprovider.api.compat.FontFamilyCompat;
import moe.shizuku.fontprovider.api.compat.TypefaceCompat;

/**
 * Created by rikka on 2017/9/27.
 */

public class TypefaceReplacer {

    private static final String TAG = "TypefaceReplacer";

    private static final String ACTION = "moe.shizuku.fontprovider.action.BIND";
    private static final String PACKAGE = "moe.shizuku.fontprovider";

    private static FontProviderServiceConnection sServiceConnection;

    public static final FontRequest NOTO_SANS_CJK_THIN;
    public static final FontRequest NOTO_SANS_CJK_LIGHT;
    public static final FontRequest NOTO_SANS_CJK_REGULAR;
    public static final FontRequest NOTO_SANS_CJK_MEDIUM;
    public static final FontRequest NOTO_SANS_CJK_BOLD;
    public static final FontRequest NOTO_SANS_CJK_BLACK;

    public static final FontRequest NOTO_SERIF_CJK_THIN;
    public static final FontRequest NOTO_SERIF_CJK_LIGHT;
    public static final FontRequest NOTO_SERIF_CJK_REGULAR;
    public static final FontRequest NOTO_SERIF_CJK_MEDIUM;
    public static final FontRequest NOTO_SERIF_CJK_BOLD;
    public static final FontRequest NOTO_SERIF_CJK_BLACK;

    public static final FontFamily NOTO_SERIF;

    public static final String[] NOTO_CJK_LANGUAGE = {"jp", "kr", "zh-Hans", "zh-Hant"};


    static {
        NOTO_SERIF = new FontFamily(null,
                new Font("NotoSerif-Regular.ttf", 0, 400, false),
                new Font("NotoSerif-Bold.ttf", 0, 700, false),
                new Font("NotoSerif-Italic.ttf", 0, 400, true),
                new Font("NotoSerif-BoldItalic.ttf", 0, 700, true)
        );

        NOTO_SANS_CJK_THIN = new FontRequest("sans-serif-thin",
                FontFamily.createFromTtc("NotoSansCJK-Light.ttc", NOTO_CJK_LANGUAGE, 100));

        NOTO_SANS_CJK_LIGHT = new FontRequest("sans-serif-light",
                FontFamily.createFromTtc("NotoSansCJK-Light.ttc", NOTO_CJK_LANGUAGE, 300));

        NOTO_SANS_CJK_REGULAR = new FontRequest("sans-serif",
                FontFamily.createFromTtc("NotoSansCJK-Regular.ttc", NOTO_CJK_LANGUAGE, 400));

        NOTO_SANS_CJK_MEDIUM = new FontRequest("sans-serif-medium",
                FontFamily.createFromTtc("NotoSansCJK-Medium.ttc", NOTO_CJK_LANGUAGE, 500));

        NOTO_SANS_CJK_BOLD = new FontRequest("sans-serif-bold",
                FontFamily.createFromTtc("NotoSansCJK-Medium.ttc", NOTO_CJK_LANGUAGE, 700));

        NOTO_SANS_CJK_BLACK = new FontRequest("sans-serif-black",
                FontFamily.createFromTtc("NotoSansCJK-Medium.ttc", NOTO_CJK_LANGUAGE, 900));

        NOTO_SERIF_CJK_THIN = new FontRequest("serif-thin", true,
                combine(NOTO_SERIF, FontFamily.createFromTtc("NotoSerifCJK-Thin.ttc", NOTO_CJK_LANGUAGE, 100)));

        NOTO_SERIF_CJK_LIGHT = new FontRequest("serif-light", true,
                combine(NOTO_SERIF, FontFamily.createFromTtc("NotoSerifCJK-Light.ttc", NOTO_CJK_LANGUAGE, 300)));

        NOTO_SERIF_CJK_REGULAR = new FontRequest("serif", true,
                combine(NOTO_SERIF, FontFamily.createFromTtc("NotoSerifCJK-Regular.ttc", NOTO_CJK_LANGUAGE, 400)));

        NOTO_SERIF_CJK_MEDIUM = new FontRequest("serif-medium", true,
                combine(NOTO_SERIF, FontFamily.createFromTtc("NotoSerifCJK-Medium.ttc", NOTO_CJK_LANGUAGE, 500)));

        NOTO_SERIF_CJK_BOLD = new FontRequest("serif-bold", true,
                combine(NOTO_SERIF, FontFamily.createFromTtc("NotoSerifCJK-Bold.ttc", NOTO_CJK_LANGUAGE, 700)));

        NOTO_SERIF_CJK_BLACK = new FontRequest("serif-black", true,
                combine(NOTO_SERIF, FontFamily.createFromTtc("NotoSerifCJK-Black.ttc", NOTO_CJK_LANGUAGE, 900)));
    }

    private static FontFamily[] combine(FontFamily first, FontFamily[] array) {
        FontFamily[] result = new FontFamily[array.length + 1];
        result[0] = first;
        System.arraycopy(array, 0, result, 1, array.length);
        return result;
    }

    private static FontFamily[] combine(FontFamily[]... arrays) {
        int length = 0;
        for (FontFamily[] array : arrays){
            length += array.length;
        }

        FontFamily[] result = new FontFamily[length];
        length = 0;
        for (FontFamily[] array : arrays) {
            System.arraycopy(array, 0, result, length, array.length);
            length += array.length;
        }
        return result;
    }

    public static class FontRequest {

        private final String name;
        private final int weight;
        private final FontFamily[] fontFamilies;
        private final boolean ignoreDefault;

        public FontRequest(String name, FontFamily... fontFamilies) {
            this(name, false, fontFamilies);
        }

        public FontRequest(String name, int weight, FontFamily... fontFamilies) {
            this(name, false, weight, fontFamilies);
        }

        public FontRequest(String name, boolean ignoreDefault, FontFamily... fontFamilies) {
            this(name, ignoreDefault, resolveWeight(name), fontFamilies);
        }

        public FontRequest(String name, boolean ignoreDefault, int weight, FontFamily... fontFamilies) {
            this.name = name;
            this.ignoreDefault = ignoreDefault;
            this.fontFamilies = fontFamilies;
            this.weight = weight;
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
            } else  {
                return 400;
            }
        }

        @Override
        public String toString() {
            return "FontRequest{" +
                    "name='" + name + '\'' +
                    ", weight=" + weight +
                    ", fontFamilies=" + Arrays.toString(fontFamilies) +
                    ", ignoreDefault=" + ignoreDefault +
                    '}';
        }
    }

    public static class FontFamily {

        private final int variant;
        private final String language;
        private final Font[] fonts;

        public static FontFamily[] createFromTtc(String filename, String[] languages) {
            return createFromTtc(filename, languages, null);
        }

        public static FontFamily[] createFromTtc(String filename, String[] languages, int weight) {
            return createFromTtc(filename, languages, null, weight, 0, false);
        }

        public static FontFamily[] createFromTtc(String filename, String[] languages, int[] ttcIndex) {
            return createFromTtc(filename, languages, ttcIndex, -1, 0, false);
        }

        public static FontFamily[] createFromTtc(String filename, String[] languages, int[] ttcIndex, int weight, int variant, boolean italic) {
            FontFamily[] fontFamilies = new FontFamily[languages.length];
            for (int i = 0; i < languages.length; i++) {
                fontFamilies[i] = new FontFamily(
                        languages[i], variant,
                        new Font(filename, ttcIndex == null ? i : ttcIndex[i], weight, italic));
            }
            return fontFamilies;
        }

        public FontFamily(String language, Font... fonts) {
            this(language, 0, fonts);
        }

        public FontFamily(String language, int variant, Font... fonts) {
            this.language = language;
            this.variant = variant;
            this.fonts = fonts;
        }

        @Override
        public String toString() {
            return "FontFamily{" +
                    "variant=" + variant +
                    ", language='" + language + '\'' +
                    ", fonts=" + Arrays.toString(fonts) +
                    '}';
        }
    }

    public static class Font {

        private final String filename;
        private final int ttcIndex;
        private final int weight;
        private final boolean italic;

        public Font(String filename) {
            this(filename, false);
        }

        public Font(String filename, boolean italic) {
            this(filename, 0, -1, italic);
        }

        public Font(String filename, int ttcIndex, int weight, boolean italic) {
            this.filename = filename;
            this.ttcIndex = ttcIndex;
            this.weight = weight;
            this.italic = italic;
        }

        @Override
        public String toString() {
            return "Font{" +
                    "filename='" + filename + '\'' +
                    ", ttcIndex=" + ttcIndex +
                    ", weight=" + weight +
                    ", italic=" + italic +
                    '}';
        }
    }

    /**
     * Replace cached or add new Typefaces in Typeface class with fonts from FontProviderService.
     *  @param context Context
     * @param fontRequests fontRequests
     */
    public static void init(Context context, FontRequest... fontRequests) {
        context = context.getApplicationContext();

        Intent intent = new Intent(ACTION)
                .setPackage(PACKAGE);

        sBufferCache.clear();
        sServiceConnection = new FontProviderServiceConnection(context, fontRequests);

        try {
            context.bindService(intent, sServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.i(TAG, "can't bindService", e);

            unbind();
        }
    }

    public static void unbind() {
        if (sServiceConnection != null) {
            sServiceConnection.unbind();
        }

        sServiceConnection = null;
    }

    private static Map<String, ByteBuffer> sBufferCache = new HashMap<>();

    static boolean request(IFontProvider fontProvider, FontRequest fontRequest) {
        Object families;
        if (!fontRequest.ignoreDefault) {
            families = Array.newInstance(FontFamilyCompat.getFontFamilyClass(), fontRequest.fontFamilies.length + 1);
        } else {
            families = Array.newInstance(FontFamilyCompat.getFontFamilyClass(), fontRequest.fontFamilies.length);
        }

        int i = 0;
        Object fallbackFonts = TypefaceCompat.getFallbackFontsArray();
        if (fallbackFonts == null
                || Array.getLength(fallbackFonts) == 0) {
            return false;
        }

        if (!fontRequest.ignoreDefault) {
            Array.set(families, i++, Array.get(fallbackFonts, 0));
        }

        for (FontFamily fontFamily : fontRequest.fontFamilies) {
            FontFamilyCompat fontFamilyCompat = new FontFamilyCompat(fontFamily.language, fontFamily.variant);
            if (fontFamilyCompat.getFontFamily() == null) {
                return false;
            }

            for (Font font : fontFamily.fonts) {
                ByteBuffer byteBuffer = sBufferCache.get(font.filename);

                if (byteBuffer == null) {
                    try {
                        ParcelFileDescriptor pfd = fontProvider.getFontFileDescriptor(font.filename);
                        if (pfd == null) {
                            Log.w(TAG, "ParcelFileDescriptor is null");
                            return false;
                        }
                        int size = fontProvider.getFontFileSize(font.filename);

                        FileInputStream is = new FileInputStream(pfd.getFileDescriptor());
                        FileChannel fileChannel = is.getChannel();
                        byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                if (byteBuffer == null) {
                    return false;
                }

                int weight = font.weight != -1 ? font.weight : fontRequest.weight;
                if (!fontFamilyCompat.addFont(byteBuffer, font.ttcIndex, weight, font.italic ? 1 : 0)) {
                    return false;
                }
            }

            if (!fontFamilyCompat.freeze()) {
                return false;
            }

            Array.set(families, i++, fontFamilyCompat.getFontFamily());
        }

        Typeface typeface = TypefaceCompat.createFromFamiliesWithDefault(families, fontRequest.weight, 0);
        if (typeface != null
                && TypefaceCompat.getSystemFontMap() != null) {
            TypefaceCompat.getSystemFontMap().put(fontRequest.name, typeface);
            return true;
        } else {
            return false;
        }
    }
}
