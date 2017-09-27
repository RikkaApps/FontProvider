package moe.shizuku.notocjk.provider.utils;

import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by rikka on 2017/9/27.
 */

public class ParcelFileDescriptorUtils {

    public static ParcelFileDescriptor dupSilently(FileDescriptor fd) {
        try {
            return ParcelFileDescriptor.dup(fd);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
