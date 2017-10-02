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

    /**/

    @Override
    public String toString() {
        return "FontRequest{" +
                "name='" + name + '\'' +
                ", weight=" + Arrays.toString(weight) +
                '}';
    }
}
