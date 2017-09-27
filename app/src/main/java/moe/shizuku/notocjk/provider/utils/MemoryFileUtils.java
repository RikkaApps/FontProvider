package moe.shizuku.notocjk.provider.utils;

import android.annotation.SuppressLint;
import android.os.MemoryFile;

import java.io.FileDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
}
