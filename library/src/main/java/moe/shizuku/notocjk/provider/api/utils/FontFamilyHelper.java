package moe.shizuku.notocjk.provider.api.utils;

import android.util.Log;

/**
 * Created by rikka on 2017/9/27.
 */

public class FontFamilyHelper {

    private static final String TAG = "TypefaceReplacer";

    public static String getFilename(String fontFamily) {
        switch (fontFamily) {
            case "sans-serif-light":
                return "NotoSansCJK-Light.ttc";
            case "sans-serif-medium":
                return "NotoSansCJK-Medium.ttc";

            case "serif-light":
                return "NotoSerifCJK-Light.ttc";
            case "serif":
                return "NotoSerifCJK-Regular.ttc";
            case "serif-medium":
                return "NotoSerifCJK-Medium.ttc";
        }

        Log.i(TAG, "requesting nonexistent font " + fontFamily);
        return null;
    }

    public static int getWeight(String fontFamily) {
        switch (fontFamily) {
            case "sans-serif":
                return 400;

            case "sans-serif-thin":
                return 100;
            case "sans-serif-light":
                return 300;
            case "sans-serif-medium":
                return 500;
            case "sans-serif-bold":
                return 700;
            case "sans-serif-black":
                return 900;

            case "serif":
                return 400;
            case "serif-light":
                return 100;
            case "serif-medium":
                return 500;
            case "serif-bold":
                return 700;
            case "serif-black":
                return 900;

            default:
                return 400;
        }
    }
}
