package moe.shizuku.notocjk.provider.api.compat;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.graphics.fonts.FontVariationAxis;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * Created by rikka on 2017/9/27.
 */

@SuppressLint("PrivateApi")
@RequiresApi(api = Build.VERSION_CODES.O)
public class FontFamilyCompat26 {

    private static boolean available = true;

    private static Class<?> cls;
    private static Constructor constructor;
    private static Method freezeMethod;
    private static Method addFontFromAssetManagerMethod;
    private static Method addFontFromBufferMethod;

    static {
        try {
            cls = Class.forName("android.graphics.FontFamily");

            constructor = cls.getDeclaredConstructor(
                    String.class, int.class);

            addFontFromAssetManagerMethod = cls.getDeclaredMethod("addFontFromAssetManager",
                    AssetManager.class, String.class, Integer.TYPE, Boolean.TYPE, Integer.TYPE,
                    Integer.TYPE, Integer.TYPE, FontVariationAxis[].class);

            addFontFromBufferMethod = cls.getDeclaredMethod("addFontFromBuffer",
                    ByteBuffer.class, Integer.TYPE, FontVariationAxis[].class, Integer.TYPE,
                    Integer.TYPE);

            freezeMethod = cls.getMethod("freeze");
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

    public static FontFamilyCompat26 create(String lang, int variant) {
        if (!available) {
            return null;
        }

        try {
            return new FontFamilyCompat26(lang, variant);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    private FontFamilyCompat26(String lang, int variant) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        mFontFamily = constructor.newInstance(lang, variant);
    }

    public Object getFontFamily() {
        return mFontFamily;
    }

    public boolean addFontFromAssetManager(AssetManager mgr, String path, int cookie,
                                           boolean isAsset, int ttcIndex, int weight, int isItalic,
                                           FontVariationAxis[] axes) {
        try {
            return (Boolean) addFontFromAssetManagerMethod.invoke(mFontFamily,
                    mgr, path, cookie, isAsset, ttcIndex, weight, isItalic, axes);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addFontFromBuffer(ByteBuffer font, int ttcIndex, FontVariationAxis[] axes,
                                     int weight, int italic) {
        try {
            return (Boolean) addFontFromBufferMethod.invoke(mFontFamily,
                    font, ttcIndex, axes, weight, italic);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean freeze() {
        try {
            return (Boolean) freezeMethod.invoke(mFontFamily);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }
}
