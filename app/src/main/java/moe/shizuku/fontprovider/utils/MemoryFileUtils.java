package moe.shizuku.fontprovider.utils;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.os.MemoryFile;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import moe.shizuku.support.utils.IOUtils;

/**
 * Created by rikka on 2017/9/27.
 */

@SuppressLint("PrivateApi")
public class MemoryFileUtils {

    private static Method getFileDescriptorMethod;

    static {
        try {
            getFileDescriptorMethod = MemoryFile.class.getDeclaredMethod("getFileDescriptor");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static FileDescriptor getFileDescriptor(MemoryFile mf) {
        if (getFileDescriptorMethod != null) {
            try {
                return  (FileDescriptor) getFileDescriptorMethod.invoke(mf);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static MemoryFile fromAsset(AssetManager am, String filename, int length) {
        MemoryFile mf = null;
        try {
            mf = new MemoryFile(filename, length);

            InputStream is = am.open(filename);
            OutputStream os = mf.getOutputStream();
            IOUtils.copy(is, os);
            return mf;
        } catch (IOException e) {
            if (mf != null) {
                mf.close();
            }
            return null;
        }
    }

    public static MemoryFile fromFile(File file) {
        MemoryFile mf = null;
        try {
            mf = new MemoryFile(file.getName(), (int) file.length());

            InputStream is = new FileInputStream(file);
            OutputStream os = mf.getOutputStream();
            IOUtils.copy(is, os);
            return mf;
        } catch (IOException e) {
            if (mf != null) {
                mf.close();
            }
            return null;
        }
    }
}
