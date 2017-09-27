package moe.shizuku.notocjk.provider.api.compat;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by rikka on 2017/9/27.
 */

@SuppressLint("PrivateApi")
@RequiresApi(api = Build.VERSION_CODES.O)
public class TypefaceCompat26 {

    private static boolean available = true;

    private static Field sFallbackFontsField;
    private static Field sSystemFontMapField;
    private static Method createFromFamiliesMethod;
    private static Method createFromFamiliesWithDefaultMethod;

    static {
        try {
            sFallbackFontsField = Typeface.class.getDeclaredField("sFallbackFonts");
            sFallbackFontsField.setAccessible(true);

            sSystemFontMapField = Typeface.class.getDeclaredField("sSystemFontMap");
            sSystemFontMapField.setAccessible(true);

            createFromFamiliesMethod = Typeface.class.getDeclaredMethod("createFromFamilies",
                    FontFamilyCompat26.ArrayClass());
            createFromFamiliesMethod.setAccessible(true);

            createFromFamiliesWithDefaultMethod = Typeface.class.getDeclaredMethod("createFromFamiliesWithDefault",
                            FontFamilyCompat26.ArrayClass(), Integer.TYPE, Integer.TYPE);
            createFromFamiliesWithDefaultMethod.setAccessible(true);
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

    public static Typeface createFromFamiliesWithDefault(Object families
            , int weight, int italic) {
        if (!available) {
            return null;
        }

        try {
            return (Typeface) createFromFamiliesWithDefaultMethod.invoke(null, families,
                    weight, italic);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
}
