package moe.shizuku.notocjk.provider.api.compat;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.graphics.fonts.FontVariationAxis;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * Created by rikka on 2017/9/27.
 */

@SuppressLint("PrivateApi")
@RequiresApi(api = Build.VERSION_CODES.O)
public class FontFamilyImpl26 implements FontFamilyImpl {

    private static boolean available = true;

    private static Constructor constructor;
    private static Method freezeMethod;
    private static Method addFontFromAssetManagerMethod;
    private static Method addFontFromBufferMethod;

    static {
        try {
            constructor = FontFamilyCompat.getFontFamilyClass().getDeclaredConstructor(
                    String.class, int.class);

            addFontFromAssetManagerMethod = FontFamilyCompat.getFontFamilyClass().getDeclaredMethod("addFontFromAssetManager",
                    AssetManager.class, String.class, Integer.TYPE, Boolean.TYPE, Integer.TYPE,
                    Integer.TYPE, Integer.TYPE, FontVariationAxis[].class);

            addFontFromBufferMethod = FontFamilyCompat.getFontFamilyClass().getDeclaredMethod("addFontFromBuffer",
                    ByteBuffer.class, Integer.TYPE, FontVariationAxis[].class, Integer.TYPE,
                    Integer.TYPE);

            freezeMethod = FontFamilyCompat.getFontFamilyClass().getMethod("freeze");
        } catch (NullPointerException | NoSuchMethodException e) {
            e.printStackTrace();

            available = false;
        }
    }

    @Override
    public Object create(String lang, int variant) {
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
    public boolean addFont(Object fontFamily, ByteBuffer font, int ttcIndex, int weight, int italic) {
        try {
            return (Boolean) addFontFromBufferMethod.invoke(fontFamily,
                    font, ttcIndex, null, weight, italic);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean freeze(Object fontFamily) {
        try {
            return (Boolean) freezeMethod.invoke(fontFamily);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*public boolean addFontFromAssetManager(AssetManager mgr, String path, int cookie,
                                           boolean isAsset, int ttcIndex, int weight, int isItalic,
                                           FontVariationAxis[] axes) {
        try {
            return (Boolean) addFontFromAssetManagerMethod.invoke(getFontFamily(),
                    mgr, path, cookie, isAsset, ttcIndex, weight, isItalic, axes);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }*/
}
