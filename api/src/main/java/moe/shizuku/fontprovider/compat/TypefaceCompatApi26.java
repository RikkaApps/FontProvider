package moe.shizuku.fontprovider.compat;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


@SuppressLint("PrivateApi")
@RequiresApi(api = Build.VERSION_CODES.O)
public class TypefaceCompatApi26 {

    private static boolean available = true;
    private static Method createFromFamiliesWithDefaultMethod;

    static {
        try {
            createFromFamiliesWithDefaultMethod = Typeface.class.getDeclaredMethod("createFromFamiliesWithDefault",
                            FontFamilyCompat.getFontFamilyArrayClass(), Integer.TYPE, Integer.TYPE);
            createFromFamiliesWithDefaultMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();

            available = false;
        }
    }

    @Nullable
    public static Typeface createFromFamiliesWithDefault(Object families, int weight, int italic) {
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
