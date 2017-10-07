package moe.shizuku.fontprovider.compat;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by rikka on 2017/9/27.
 */

@SuppressLint("PrivateApi")
@RequiresApi(api = Build.VERSION_CODES.N)
public class TypefaceCompatApi21 extends TypefaceCompat {

    private static boolean available = true;

    private static Method createFromFamiliesWithDefaultMethod;
    private static Method nativeCreateWeightAliasMethod;
    private static Constructor constructor;
    private static Field nativeInstanceField;

    static {
        try {
            createFromFamiliesWithDefaultMethod = Typeface.class.getDeclaredMethod("createFromFamiliesWithDefault",
                    FontFamilyCompat.getFontFamilyArrayClass());
            createFromFamiliesWithDefaultMethod.setAccessible(true);

            constructor = Typeface.class.getDeclaredConstructor(Long.TYPE);
            constructor.setAccessible(true);

            nativeCreateWeightAliasMethod = Typeface.class.getDeclaredMethod("nativeCreateWeightAlias",
                    Long.TYPE, Integer.TYPE);
            nativeCreateWeightAliasMethod.setAccessible(true);

            nativeInstanceField = Typeface.class.getDeclaredField("native_instance");
            nativeInstanceField.setAccessible(true);
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            e.printStackTrace();

            available = false;
        }
    }

    public static Typeface createFromFamiliesWithDefault(Object families, int weight) {
        if (!available) {
            return null;
        }

        try {
            Typeface base = (Typeface) createFromFamiliesWithDefaultMethod.invoke(null, families);
            long native_instance = (long) nativeCreateWeightAliasMethod.invoke(null, getNativeInstance(base), weight);
            return (Typeface) constructor.newInstance(native_instance);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static long getNativeInstance(Typeface typeface) throws IllegalAccessException {
        return (long) nativeInstanceField.get(typeface);
    }
}
