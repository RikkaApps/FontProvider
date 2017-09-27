package moe.shizuku.notocjk.provider.api.compat;

import android.annotation.SuppressLint;
import android.os.Build;

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

    private Object mFontFamily;

    public Object getFontFamily() {
        return mFontFamily;
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
            throw new IllegalStateException("unsupported system version");
        }
        return sImpl;
    }


    public FontFamilyCompat(String lang, int variant) {
        this(getImpl().create(lang, variant));
    }

    private FontFamilyCompat(Object fontFamily) {
        mFontFamily = fontFamily;
    }

    public boolean addFont(ByteBuffer font, int ttcIndex, int weight, int italic) {
        return getImpl().addFont(getFontFamily(), font, ttcIndex, weight, italic);
    }

    public boolean freeze() {
        return getImpl().freeze(getFontFamily());
    }
}
