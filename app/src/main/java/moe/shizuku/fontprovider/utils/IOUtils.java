package moe.shizuku.fontprovider.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by rikka on 2017/9/28.
 */

public class IOUtils {

    public static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] b = new byte[8192];
        for (int r; (r = is.read(b)) != -1;) {
            os.write(b, 0, r);
        }
    }
}
