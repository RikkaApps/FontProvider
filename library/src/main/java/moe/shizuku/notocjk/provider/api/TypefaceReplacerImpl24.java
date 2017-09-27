package moe.shizuku.notocjk.provider.api;

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
import moe.shizuku.notocjk.provider.api.compat.FontFamilyCompat24;
import moe.shizuku.notocjk.provider.api.compat.TypefaceCompat24;
import moe.shizuku.notocjk.provider.api.utils.FontFamilyHelper;

/**
 * Created by rikka on 2017/9/27.
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class TypefaceReplacerImpl24 extends TypefaceReplacerImpl  {

    private static final String TAG = "TypefaceReplacerImpl24";

    private static FontFamilyCompat24 addFontWeightStyle(ByteBuffer buffer, String lang, int weight, int ttcIndex, boolean style) {
        FontFamilyCompat24 fontFamily = FontFamilyCompat24.create(lang, null);
        if (fontFamily == null) {
            return null;
        }

        if (!fontFamily.addFontWeightStyle(
                buffer, ttcIndex, null, weight, style)) {
            return null;
        }

        return fontFamily;
    }

    public static boolean replace(ByteBuffer font, String fontFamily) {
        int weight = FontFamilyHelper.getWeight(fontFamily);

        FontFamilyCompat24[] fontFamilyCompat = new FontFamilyCompat24[4];
        fontFamilyCompat[0] = addFontWeightStyle(font, "zh-Hans", weight, 2, false);
        fontFamilyCompat[1] = addFontWeightStyle(font, "zh-Hant", weight, 3, false);
        fontFamilyCompat[2] = addFontWeightStyle(font, "ja", weight, 0, false);
        fontFamilyCompat[3] = addFontWeightStyle(font, "kr", weight, 1, false);

        boolean serif = !fontFamily.startsWith("sans-serif");

        Object families = Array.newInstance(FontFamilyCompat24.Class(), serif ? 4 : 5);

        Object fallbackFonts = TypefaceCompat24.getFallbackFontsArray();
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

        Typeface typeface = TypefaceCompat24.createFromFamiliesWithDefault(families);
        if (typeface != null
                && TypefaceCompat24.getSystemFontMap() != null) {
            TypefaceCompat24.getSystemFontMap().put(fontFamily, typeface);
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

            return TypefaceReplacerImpl24.replace(byteBuffer, family);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
