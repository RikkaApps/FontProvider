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
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TypefaceCompatApi21 extends TypefaceCompat {

    private static boolean available = true;

    private static Method createFromFamiliesWithDefaultMethod;

    static {
        try {
            createFromFamiliesWithDefaultMethod = Typeface.class.getDeclaredMethod("createFromFamiliesWithDefault",
                    FontFamilyCompat.getFontFamilyArrayClass());
            createFromFamiliesWithDefaultMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();

            available = false;
        }
    }

    public static Typeface createFromFamiliesWithDefault(Object families/*, int weight*/) {
        if (!available) {
            return null;
        }

        try {
            return (Typeface) createFromFamiliesWithDefaultMethod.invoke(null, families);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
}
