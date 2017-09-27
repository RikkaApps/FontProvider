package moe.shizuku.notocjk.provider.api;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import moe.shizuku.notocjk.provider.IFontProvider;
import moe.shizuku.notocjk.provider.api.compat.FontFamilyCompat26;
import moe.shizuku.notocjk.provider.api.compat.TypefaceCompat26;
import moe.shizuku.notocjk.provider.api.utils.FontFamilyHelper;

/**
 * Created by rikka on 2017/9/26.
 */

@SuppressLint("PrivateApi")
@RequiresApi(api = Build.VERSION_CODES.O)
public class TypefaceReplacerImpl26 extends TypefaceReplacerImpl {

    private static final String TAG = "TypefaceReplacerImpl26";

    private static FontFamilyCompat26 createFamilyFromBuffer(ByteBuffer buffer, String lang, int weight, int ttcIndex, int isItalic) {
        FontFamilyCompat26 fontFamily = FontFamilyCompat26.create(lang, 0);
        if (fontFamily == null) {
            return null;
        }

        if (fontFamily.addFontFromBuffer(
                buffer, ttcIndex, null, weight, isItalic)) {
            if (!fontFamily.freeze()) {
                return null;
            }
        }

        return fontFamily;
    }

    public static boolean replace(ByteBuffer font, String fontFamily) {
        int weight = FontFamilyHelper.getWeight(fontFamily);

        FontFamilyCompat26[] fontFamilyCompat = new FontFamilyCompat26[4];
        fontFamilyCompat[0] = createFamilyFromBuffer(font, "zh-Hans", weight, 2, 0);
        fontFamilyCompat[1] = createFamilyFromBuffer(font, "zh-Hant", weight, 3, 0);
        fontFamilyCompat[2] = createFamilyFromBuffer(font, "ja", weight, 0, 0);
        fontFamilyCompat[3] = createFamilyFromBuffer(font, "kr", weight, 1, 0);

        boolean serif = !fontFamily.startsWith("sans-serif");

        Object families = Array.newInstance(FontFamilyCompat26.Class(), serif ? 4 : 5);

        Object fallbackFonts = TypefaceCompat26.getFallbackFontsArray();
        if (fallbackFonts != null) {
            // to serif fonts we don't need put sans-serif first
            int i = serif ? -1 : 0;
            if (!serif) {
                Array.set(families, 0, Array.get(fallbackFonts, 0));
            }
            Array.set(families, 1 + i, fontFamilyCompat[0].getFontFamily());
            Array.set(families, 2 + i, fontFamilyCompat[1].getFontFamily());
            Array.set(families, 3 + i, fontFamilyCompat[2].getFontFamily());
            Array.set(families, 4 + i, fontFamilyCompat[3].getFontFamily());
        }

        Typeface typeface = TypefaceCompat26.createFromFamiliesWithDefault(families, weight, 0);
        if (typeface != null
                && TypefaceCompat26.getSystemFontMap() != null) {
            TypefaceCompat26.getSystemFontMap().put(fontFamily, typeface);
            return true;
        }
        return false;
    }

    @Override
    public boolean replace(IFontProvider fontProvider, String family) {
        String filename = FontFamilyHelper.getFilename(family);

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

            return TypefaceReplacerImpl26.replace(byteBuffer, family);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
