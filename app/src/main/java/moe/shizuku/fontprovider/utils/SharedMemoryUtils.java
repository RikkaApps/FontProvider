package moe.shizuku.fontprovider.utils;

import android.os.SharedMemory;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

public class SharedMemoryUtils {

    public static SharedMemory fromFile(File file) {
        SharedMemory sm = null;
        try {
            sm = SharedMemory.create(file.getName(), (int) file.length());

            DataInputStream is = new DataInputStream(new FileInputStream(file));
            byte[] bytes = new byte[(int) file.length()];
            is.readFully(bytes);
            sm.mapReadWrite().put(bytes);

            return sm;
        } catch (Exception e) {
            e.printStackTrace();
            if (sm != null) {
                sm.close();
            }
            return null;
        }
    }

}
