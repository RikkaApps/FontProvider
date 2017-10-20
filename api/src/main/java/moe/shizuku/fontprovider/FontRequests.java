package moe.shizuku.fontprovider;

import android.text.TextUtils;

import java.util.Arrays;

/**
 * Created by rikka on 2017/10/1.
 */

public class FontRequests {

    private static int[] resolveWeight(String name) {
        if (TextUtils.isEmpty(name)) {
            return new int[]{400, 700};
        }

        if (name.endsWith("-thin")) {
            return new int[]{100};
        } else if (name.endsWith("-demilight")) {
            return new int[]{200};
        } else if (name.endsWith("-light")) {
            return new int[]{300};
        } else if (name.endsWith("-medium")) {
            return new int[]{500};
        } else if (name.endsWith("-bold")) {
            return new int[]{700};
        } else if (name.endsWith("-black")) {
            return new int[]{900};
        } else {
            return new int[]{400, 700};
        }
    }

    private static boolean resolveIsSerif(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        return name.startsWith("serif");
    }

    public static FontRequest[] DEFAULT_SANS_SERIF_FONTS = new FontRequest[]{FontRequest.DEFAULT};

    public static FontRequest[] DEFAULT_SERIF_FONTS = new FontRequest[]{FontRequest.NOTO_SERIF};

    public static void setDefaultSansSerifFonts(FontRequest... fonts) {
        DEFAULT_SANS_SERIF_FONTS = fonts;
    }

    public static void setDefaultSerifFonts(FontRequest... fonts) {
        DEFAULT_SERIF_FONTS = fonts;
    }

    private static FontRequest[] getDefaultFont(String name) {
        return resolveIsSerif(name) ? DEFAULT_SERIF_FONTS : DEFAULT_SANS_SERIF_FONTS;
    }

    public static FontRequests create(String name, String fontName) {
        return FontRequests.create(name, fontName, resolveWeight(name));
    }

    public static FontRequests create(String name, String fontName, int... weight) {
        return FontRequests.create(getDefaultFont(name), fontName, weight);
    }

    public static FontRequests create(FontRequest[] defaultFonts, String name, int... weight) {
        return new FontRequests(weight,
                FontRequest.combine(defaultFonts, new FontRequest[]{new FontRequest(name, weight)})
        );
    }

    public final int[] weight;
    public final FontRequest[] requests;

    public FontRequests(int weight, FontRequest... requests) {
        this(new int[]{weight}, requests);
    }

    public FontRequests(int[] weight, FontRequest... requests) {
        this.weight = weight;
        this.requests = requests;
    }

    @Override
    public String toString() {
        return "FontRequests{" +
                "weight=" + weight +
                ", requests=" + Arrays.toString(requests) +
                '}';
    }
}
