package moe.shizuku.fontprovider.compat;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.File;
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
public class FontFamilyImpl21 implements FontFamilyImpl {

    private static boolean available = true;

    private static Constructor constructor;
    private static Method addFontWeightStyleMethod;

    static {
        try {
            constructor = FontFamilyCompat.getFontFamilyClass().getDeclaredConstructor(
                    String.class, String.class);

            addFontWeightStyleMethod = FontFamilyCompat.getFontFamilyClass().getDeclaredMethod("addFontWeightStyle",
                    String.class, Integer.TYPE, Boolean.TYPE);
        } catch (NullPointerException | NoSuchMethodException e) {
            e.printStackTrace();

            available = false;
        }
    }

    public Object create(String lang, String variant) {
        if (!available) {
            return null;
        }

        try {
            return constructor.newInstance(lang, variant);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    @Override
    public Object create(String lang, int variant) {
        String varEnum = null;
        if (variant == 1) {
            varEnum = "compact";
        } else if (variant == 2) {
            varEnum = "elegant";
        }
        return create(lang, varEnum);
    }

    @Override
    public boolean addFont(Object fontFamily, ByteBuffer font, int ttcIndex, int weight, int italic) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addFont(Object fontFamily, String path, int weight, int italic) {
        try {
            return (Boolean) addFontWeightStyleMethod.invoke(fontFamily,
                    path, weight, italic == 1);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean freeze(Object fontFamily) {
        return true;
    }
}
