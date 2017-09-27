package moe.shizuku.notocjk.provider.api;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import moe.shizuku.notocjk.provider.IFontProvider;
import moe.shizuku.notocjk.provider.api.compat.FontFamilyCompat;
import moe.shizuku.notocjk.provider.api.compat.TypefaceCompat;

/**
 * Created by rikka on 2017/9/27.
 */

public class TypefaceReplacer {

    private static final String TAG = "TypefaceReplacer";

    private static final String ACTION = "moe.shizuku.notocjk.provider.FontProviderService.BIND";
    private static final String PACKAGE = "moe.shizuku.notocjk.provider";

    /**
     * Replace cached Typefaces in Typeface class with fonts from FontProviderService.
     *
     * @param context Context
     * @param requestFonts fontFamilies to be replace
     */
    public static void init(Context context, String[] requestFonts) {
        Intent intent = new Intent(ACTION)
                .setPackage(PACKAGE);

        try {
            context.bindService(intent, new FontProviderServiceConnection(requestFonts), Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.i(TAG, "can't bindService", e);
        }
    }

    private static final String[] sIndexToLang = new String[]{"zh-Hans", "zh-Hant", "ja", "kr"};
    private static final int[] sIndexToTTCIndex = new int[]{2, 3, 0, 1};

    static boolean replace(IFontProvider fontProvider, String family) {
        String filename = toFontFilename(family);

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

            return replace(byteBuffer, family);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

    private static boolean replace(ByteBuffer font, String fontFamily) {
        int weight = toWeight(fontFamily);

        Object fallbackFonts = TypefaceCompat.getFallbackFontsArray();
        if (fallbackFonts == null) {
            return false;
        }

        boolean serif = isSerif(fontFamily);
        int length = serif ? 4 : 5;
        int start = serif ? 0 : 1;

        Object families = Array.newInstance(FontFamilyCompat.getFontFamilyClass(), length);

        if (!serif) {
            Array.set(families, 0, Array.get(fallbackFonts, 0));
        }

        for (int i = 0; start < length; start++, i++) {
            FontFamilyCompat fontFamilyCompat = createFontFamily(font, sIndexToLang[i], weight, sIndexToTTCIndex[i], 0);
            if (fontFamilyCompat == null) {
                return false;
            }
            Array.set(families, start, fontFamilyCompat.getFontFamily());
        }

        Typeface typeface = TypefaceCompat.createFromFamiliesWithDefault(families, weight, 0);
        if (typeface != null
                && TypefaceCompat.getSystemFontMap() != null) {
            TypefaceCompat.getSystemFontMap().put(fontFamily, typeface);
            return true;
        }
        return false;
    }

    private static String toFontFilename(String fontFamily) {
        switch (fontFamily) {
            case "sans-serif-light":
                return "NotoSansCJK-Light.ttc";
            case "sans-serif-medium":
                return "NotoSansCJK-Medium.ttc";

            case "serif-light":
                return "NotoSerifCJK-Light.ttc";
            case "serif":
                return "NotoSerifCJK-Regular.ttc";
            case "serif-medium":
                return "NotoSerifCJK-Medium.ttc";
        }

        Log.i(TAG, "requesting nonexistent font " + fontFamily);
        return null;
    }

    private static int toWeight(String fontFamily) {
        switch (fontFamily) {
            case "sans-serif":
                return 400;

            case "sans-serif-thin":
                return 100;
            case "sans-serif-light":
                return 300;
            case "sans-serif-medium":
                return 500;
            case "sans-serif-bold":
                return 700;
            case "sans-serif-black":
                return 900;

            case "serif":
                return 400;
            case "serif-light":
                return 100;
            case "serif-medium":
                return 500;
            case "serif-bold":
                return 700;
            case "serif-black":
                return 900;

            default:
                return 400;
        }
    }

    private static boolean isSerif(String fontFamily) {
        return fontFamily.startsWith("serif");
    }
}
