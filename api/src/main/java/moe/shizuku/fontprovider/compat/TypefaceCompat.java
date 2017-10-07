package moe.shizuku.fontprovider.compat;

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
    private static Method setDefaultMethod;

    static {
        try {
            sFallbackFontsField = Typeface.class.getDeclaredField("sFallbackFonts");
            sFallbackFontsField.setAccessible(true);

            sSystemFontMapField = Typeface.class.getDeclaredField("sSystemFontMap");
            sSystemFontMapField.setAccessible(true);

            createFromFamiliesMethod = Typeface.class.getDeclaredMethod("createFromFamilies",
                    FontFamilyCompat.getFontFamilyArrayClass());
            createFromFamiliesMethod.setAccessible(true);

            setDefaultMethod = Typeface.class.getDeclaredMethod("setDefault",
                    Typeface.class);
            setDefaultMethod.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            e.printStackTrace();

            available = false;
        }
    }

    /**
     * Return Typeface.sFallbackFonts.
     */
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

    /**
     * Return Typeface.sSystemFontMap.
     */
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

    public static void setDefault(Typeface typeface) {
        if (!available) {
            return;
        }

        try {
            setDefaultMethod.invoke(null, typeface);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create Typeface with the order of your fontFamilies.
     *
     * @param families FontFamily array Object
     * @return Typeface object
     */
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

    /**
     * Create a new typeface from an array of font families, including
     * also the font families in the fallback list.
     * @param weight the weight for this family, required on API 26+.
     * @param italic the italic information for this family, required on API 26+.
     * @param families array of font families.
     * @return Typeface object
     */
    public static Typeface createFromFamiliesWithDefault(Object families, int weight, int italic) {
        if (Build.VERSION.SDK_INT >= 26) {
            return TypefaceCompatApi26.createFromFamiliesWithDefault(families, weight, italic);
        } else if (Build.VERSION.SDK_INT >= 21) {
            return TypefaceCompatApi21.createFromFamiliesWithDefault(families, weight);
        } else {
            throw new IllegalStateException("unsupported system version");
        }
    }
}
