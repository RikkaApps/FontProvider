package moe.shizuku.notocjk.provider.api.compat;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by rikka on 2017/9/27.
 */

@SuppressLint("PrivateApi")
public class TypefaceCompat {

    private static boolean available = true;

    private static Field sFallbackFontsField;
    private static Field sSystemFontMapField;
    private static Method createFromFamiliesMethod;

    static {
        try {
            sFallbackFontsField = Typeface.class.getDeclaredField("sFallbackFonts");
            sFallbackFontsField.setAccessible(true);

            sSystemFontMapField = Typeface.class.getDeclaredField("sSystemFontMap");
            sSystemFontMapField.setAccessible(true);

            createFromFamiliesMethod = Typeface.class.getDeclaredMethod("createFromFamilies",
                    FontFamilyCompat.getFontFamilyArrayClass());
            createFromFamiliesMethod.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            e.printStackTrace();

            available = false;
        }
    }

    public static Object getFallbackFontsArray() {
        if (!available) {
            return null;
        }

        try {
            return sFallbackFontsField.get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Typeface> getSystemFontMap() {
        if (!available) {
            return null;
        }

        try {
            return (Map<String, Typeface>) sSystemFontMapField.get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Typeface createFromFamilies(Object families) {
        if (!available) {
            return null;
        }

        try {
            return (Typeface) createFromFamiliesMethod.invoke(null, families);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Typeface createFromFamiliesWithDefault(Object families, int weight, int italic) {
        if (Build.VERSION.SDK_INT >= 26) {
            return TypefaceCompatApi26.createFromFamiliesWithDefault(families, weight, italic);
        } else if (Build.VERSION.SDK_INT >= 24) {
            return TypefaceCompatApi24.createFromFamiliesWithDefault(families);
        } else {
            throw new IllegalStateException("unsupported system version");
        }
    }
}
