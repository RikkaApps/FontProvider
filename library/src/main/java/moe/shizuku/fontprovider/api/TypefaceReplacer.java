package moe.shizuku.fontprovider.api;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

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

    public static final FontRequest NOTO_SANS_CJK_LIGHT;
    public static final FontRequest NOTO_SANS_CJK_MEDIUM;
    public static final FontRequest NOTO_SERIF_CJK_LIGHT;
    public static final FontRequest NOTO_SERIF_CJK_REGULAR;
    public static final FontRequest NOTO_SERIF_CJK_MEDIUM;

    static {
        NOTO_SANS_CJK_LIGHT = new FontRequest(
                "sans-serif-light",
                "NotoSansCJK-Light.ttc",
                new FontInfo(300, "ja", "kr", "zh-Hans", "zh-Hant")
        );

        NOTO_SANS_CJK_MEDIUM = new FontRequest(
                "sans-serif-medium",
                "NotoSansCJK-Medium.ttc",
                new FontInfo(500, "ja", "kr", "zh-Hans", "zh-Hant")
        );

        NOTO_SERIF_CJK_LIGHT = new FontRequest(
                "serif-light",
                "NotoSerifCJK-Light.ttc",
                new FontInfo(300, true, "ja", "kr", "zh-Hans", "zh-Hant")
        );

        NOTO_SERIF_CJK_REGULAR = new FontRequest(
                "serif",
                "NotoSerifCJK-Regular.ttc",
                new FontInfo(400, true, "ja", "kr", "zh-Hans", "zh-Hant")
        );

        NOTO_SERIF_CJK_MEDIUM = new FontRequest(
                "serif-medium",
                "NotoSerifCJK-Medium.ttc",
                new FontInfo(500, true, "ja", "kr", "zh-Hans", "zh-Hant")
        );
    }

    public static class FontRequest {

        private final String fontFamily;
        private final String filename;
        private final FontInfo fontInfo;

        public FontRequest(String fontFamily, String filename, FontInfo fontInfo) {
            this.fontFamily = fontFamily;
            this.filename = filename;
            this.fontInfo = fontInfo;
        }

        @Override
        public String toString() {
            return "FontRequest{" +
                    "fontFamily='" + fontFamily + '\'' +
                    ", filename='" + filename + '\'' +
                    ", fontInfo=" + fontInfo +
                    '}';
        }
    }

    public static class FontInfo {

        private final boolean serif;
        private final int weight;
        private final boolean isItalic;
        private final String[] languages;

        public FontInfo(int weight, String... languages) {
            this(weight, false, languages);
        }

        public FontInfo(int weight, boolean serif, String... languages) {
            this(weight, serif, false, languages);
        }

        public FontInfo(int weight, boolean serif, boolean isItalic, String... languages) {
            this.serif = serif;
            this.weight = weight;
            this.languages = languages;
            this.isItalic = isItalic;
        }
    }

    /**
     * Replace cached or add new Typefaces in Typeface class with fonts from FontProviderService.
     *
     * @param context Context
     * @param fontRequests fontRequests
     */
    public static void init(Context context, FontRequest... fontRequests) {
        context = context.getApplicationContext();

        Intent intent = new Intent(ACTION)
                .setPackage(PACKAGE);

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

    static boolean replace(IFontProvider fontProvider, FontRequest request) {
        String filename = request.filename;

        if (filename == null) {
            return false;
        }

        try {
            ParcelFileDescriptor pfd = fontProvider.getFontFileDescriptor(filename);
            int size = fontProvider.getFontFileSize(filename);
            if (pfd == null) {
                Log.w(TAG, "ParcelFileDescriptor is null");
                return false;
            }

            FileInputStream is = new FileInputStream(pfd.getFileDescriptor());
            FileChannel fileChannel = is.getChannel();
            ByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);

            return replace(byteBuffer, request);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean replace(ByteBuffer buffer, FontRequest request) {
        FontInfo fontInfo = request.fontInfo;
        int weight = fontInfo.weight;

        Object fallbackFonts = TypefaceCompat.getFallbackFontsArray();
        if (fallbackFonts == null) {
            return false;
        }

        boolean serif = fontInfo.serif;
        int length = fontInfo.languages.length;
        length += serif ? 0 : 1;
        int index = serif ? 0 : 1;

        Object families = Array.newInstance(FontFamilyCompat.getFontFamilyClass(), length);

        // if not serif, 0 (san-serif) need to be the first
        if (!serif) {
            Array.set(families, 0, Array.get(fallbackFonts, 0));
        }

        for (int count = 0; index < length; index++, count++) {
            FontFamilyCompat fontFamilyCompat = createFontFamily(buffer, fontInfo.languages[count], weight, count, fontInfo.isItalic ? 1 : 0);
            if (fontFamilyCompat == null) {
                return false;
            }
            Array.set(families, index, fontFamilyCompat.getFontFamily());
        }

        Typeface typeface = TypefaceCompat.createFromFamiliesWithDefault(families, weight, 0);
        if (typeface != null
                && TypefaceCompat.getSystemFontMap() != null) {
            TypefaceCompat.getSystemFontMap().put(request.fontFamily, typeface);
            return true;
        }
        return false;
    }

    private static FontFamilyCompat createFontFamily(ByteBuffer buffer, String lang, int weight, int ttcIndex, int isItalic) {
        FontFamilyCompat fontFamily = new FontFamilyCompat(lang, 0);
        if (fontFamily.getFontFamily() == null) {
            return null;
        }

        if (fontFamily.addFont(buffer, ttcIndex, weight, isItalic)) {
            if (!fontFamily.freeze()) {
                return null;
            }
        }

        return fontFamily;
    }
}
