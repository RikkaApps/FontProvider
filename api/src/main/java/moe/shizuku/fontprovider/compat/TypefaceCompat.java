package moe.shizuku.fontprovider.compat;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.Nullable;

import java.lang.reflect.Constructor;
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
    private static Method nativeCreateWeightAliasMethod;
    private static Constructor constructor;
    private static Field nativeInstanceField;

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

            nativeCreateWeightAliasMethod = Typeface.class.getDeclaredMethod("nativeCreateWeightAlias",
                    Long.TYPE, Integer.TYPE);
            nativeCreateWeightAliasMethod.setAccessible(true);

            constructor = Typeface.class.getDeclaredConstructor(Long.TYPE);
            constructor.setAccessible(true);

            nativeInstanceField = Typeface.class.getDeclaredField("native_instance");
            nativeInstanceField.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            e.printStackTrace();

            available = false;
        }
    }

    /**
     * Return Typeface.sFallbackFonts.
     *
     * @return Typeface.sFallbackFonts
     */
    @Nullable
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
     * Set Typeface.sFallbackFonts
     *
     * @param array FontFamily[]
     */
    public static void setFallbackFontsArray(Object array) {
        if (!available) {
            return;
        }

        try {
            sFallbackFontsField.set(null, array);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Typeface> sSystemFontMap;

    /**
     * Return Typeface.sSystemFontMap.
     *
     * @return Typeface.sSystemFontMap
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static Map<String, Typeface> getSystemFontMap() {
        if (!available) {
            return null;
        }

        if (sSystemFontMap == null) {
            try {
                sSystemFontMap = (Map<String, Typeface>) sSystemFontMapField.get(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }

        return sSystemFontMap;
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

    private static long nativeCreateWeightAlias(long native_instance, int weight) {
        if (!available) {
            return -1;
        }

        try {
            return (long) nativeCreateWeightAliasMethod.invoke(null, native_instance, weight);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Nullable
    public static Typeface create(long ni) {
        if (!available) {
            return null;
        }

        try {
            return (Typeface) constructor.newInstance(ni);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create new Typeface instance with weigh alias from given Typeface.
     * <br>
     * Use this on pre-Oreo to not breaking font fallback of other languages.
     *
     * @param family base Typeface
     * @param weight font weight
     * @return new Typeface
     */
    public static Typeface createWeightAlias(Typeface family, int weight) {
        if (!available) {
            return family;
        }

        try {
            return (Typeface) constructor.newInstance(nativeCreateWeightAlias(getNativeInstance(family), weight));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return family;
        }
    }

    /**
     * Create Typeface with the order of your fontFamilies.
     *
     * @param families FontFamily array Object
     * @return Typeface object
     */
    @Nullable
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
     *
     * @param weight the weight for this family, only required on API 26+.
     * @param italic the italic information for this family, only required on API 26+.
     * @param families array of font families.
     * @return Typeface object
     */
    @Nullable
    public static Typeface createFromFamiliesWithDefault(Object families, int weight, int italic) {
        if (Build.VERSION.SDK_INT >= 26) {
            return TypefaceCompatApi26.createFromFamiliesWithDefault(families, weight, italic);
        } else {
            return TypefaceCompatApi21.createFromFamiliesWithDefault(families);
        }
    }

    /**
     * Get native_instance of a Typeface.
     *
     * @param typeface Typeface instance
     * @return Typeface.native_instance
     * @throws IllegalAccessException native_instance not accessible
     */
    public static long getNativeInstance(Typeface typeface) throws IllegalAccessException {
        return (long) nativeInstanceField.get(typeface);
    }
}
