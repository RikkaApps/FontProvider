package moe.shizuku.fontprovider.compat;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;

/**
 * Created by rikka on 2017/9/27.
 */

@SuppressLint("PrivateApi")
public class FontFamilyCompat {

    private static Class<?> cls;

    static {
        try {
            cls = Class.forName("android.graphics.FontFamily");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Class<?> getFontFamilyClass() {
        return cls;
    }

    public static Class<?> getFontFamilyArrayClass() {
        return Array.newInstance(cls, 0).getClass();
    }

    private static FontFamilyImpl sImpl;

    private static FontFamilyImpl getImpl() {
        if (sImpl != null) {
            return sImpl;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sImpl = new FontFamilyImpl26();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sImpl = new FontFamilyImpl24();
        } else {
            sImpl = new FontFamilyImpl21();
        }
        return sImpl;
    }

    private Object mFontFamily;

    /**
     * Create FontFamily with lang and variant.
     *
     * @param lang Language
     * @param variant Variant (0, 1 - compact, 2 - elegant)
     */
    public FontFamilyCompat(String lang, int variant) {
        this(getImpl().create(lang, variant));
    }

    private FontFamilyCompat(Object fontFamily) {
        mFontFamily = fontFamily;
    }


    /**
     * Get real android.graphics.FontFamily object.
     *
     * @return FontFamily object
     */
    public Object getFontFamily() {
        return mFontFamily;
    }

    /**
     * Add font to this FontFamily.
     *
     * @param font font file buffer
     * @param ttcIndex ttc index
     * @param weight The weight of the font.
     * @param italic Whether this font is italic.
     * @return returns false if some error happens in native code.
     */
    public boolean addFont(ByteBuffer font, int ttcIndex, int weight, int italic) {
        return getImpl().addFont(getFontFamily(), font, ttcIndex, weight, italic);
    }

    /**
     * Add font to this FontFamily.
     *
     * @param path font file path
     * @param weight The weight of the font.
     * @param italic Whether this font is italic.
     * @return returns false if some error happens in native code.
     */
    public boolean addFont(String path, int weight, int italic) {
        return getImpl().addFont(getFontFamily(), path, weight, italic);
    }

    /**
     * Finalize the FontFamily creation, always return true on pre-API 26.
     *
     * @return boolean returns false if some error happens in native code, e.g. broken font file is
     *                 passed, etc.
     */
    public boolean freeze() {
        return getImpl().freeze(getFontFamily());
    }
}
