package moe.shizuku.notocjk.provider.api.compat;

import android.annotation.SuppressLint;
import android.graphics.fonts.FontVariationAxis;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by rikka on 2017/9/27.
 */

@SuppressLint("PrivateApi")
@RequiresApi(api = Build.VERSION_CODES.N)
public class FontFamilyCompat24 {

    private static boolean available = true;

    private static Class<?> cls;
    private static Constructor constructor;
    private static Method addFontWeightStyleMethod;

    static {
        try {
            cls = Class.forName("android.graphics.FontFamily");

            constructor = cls.getDeclaredConstructor(
                    String.class, String.class);

            addFontWeightStyleMethod = cls.getDeclaredMethod("addFontWeightStyle",
                    ByteBuffer.class, Integer.TYPE, List.class, Integer.TYPE,
                    Boolean.TYPE);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();

            available = false;
        }
    }

    public static Class Class() {
        return cls;
    }

    public static Class ArrayClass() {
        return Array.newInstance(cls, 0).getClass();
    }

    private Object mFontFamily;

    public static FontFamilyCompat24 create(String lang, String variant) {
        if (!available) {
            return null;
        }

        try {
            return new FontFamilyCompat24(lang, variant);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    private FontFamilyCompat24(String lang, String variant) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        mFontFamily = constructor.newInstance(lang, variant);
    }

    public Object getFontFamily() {
        return mFontFamily;
    }

    public boolean addFontWeightStyle(ByteBuffer font, int ttcIndex, FontVariationAxis[] axes,
                                     int weight, boolean style) {
        try {
            return (Boolean) addFontWeightStyleMethod.invoke(mFontFamily,
                    font, ttcIndex, axes, weight, style);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }
}
