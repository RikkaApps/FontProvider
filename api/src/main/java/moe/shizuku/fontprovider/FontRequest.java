package moe.shizuku.fontprovider;

import android.os.RemoteException;

import java.util.Arrays;

import moe.shizuku.fontprovider.font.Font;
import moe.shizuku.fontprovider.font.FontFamily;

/**
 * Created by rikka on 2017/9/30.
 */
public class FontRequest {

    public static final FontRequest DEFAULT = new FontRequest();

    public static final FontRequest NOTO_SERIF = new FontRequest("Noto Serif", 400, 700);

    public final String name;
    public final int[] weight;

    private FontRequest() {
        this(null, null);
    }

    public FontRequest(String name, int... weight) {
        this.name = name;
        this.weight = weight;
    }

    public FontFamily[] getFontFamily(IFontProvider fontProvider) throws RemoteException {
        return fontProvider.getFontFamily(name, weight);
    }

    /*private static int resolveWeight(String name) {
        if (TextUtils.isEmpty(name)) {
            return 400;
        }

        if (name.endsWith("-thin")) {
            return 100;
        } else if (name.endsWith("-demilight")) {
            return 200;
        } else if (name.endsWith("-light")) {
            return 300;
        } else if (name.endsWith("-medium")) {
            return 500;
        } else if (name.endsWith("-bold")) {
            return 700;
        } else if (name.endsWith("-black")) {
            return 900;
        } else {
            return 400;
        }
    }*/

    @Override
    public String toString() {
        return "FontRequest{" +
                "name='" + name + '\'' +
                ", weight=" + Arrays.toString(weight) +
                '}';
    }
}
