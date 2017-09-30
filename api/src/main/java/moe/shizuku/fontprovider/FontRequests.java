package moe.shizuku.fontprovider;

import java.util.Arrays;

/**
 * Created by rikka on 2017/10/1.
 */

public class FontRequests {

    // Noto Sans CJK
    public static final FontRequests NOTO_SANS_CJK_THIN = new FontRequests(
            100,
            FontRequest.DEFAULT,
            new FontRequest("Noto Sans CJK", 100));

    public static final FontRequests NOTO_SANS_CJK_LIGHT = new FontRequests(
            300,
            FontRequest.DEFAULT,
            new FontRequest("Noto Sans CJK", 300));

    public static final FontRequests NOTO_SANS_CJK_REGULAR = new FontRequests(
            400,
            FontRequest.DEFAULT,
            new FontRequest("Noto Sans CJK", 400),
            new FontRequest("Noto Sans CJK", 700));

    public static final FontRequests NOTO_SANS_CJK_MEDIUM = new FontRequests(
            500,
            FontRequest.DEFAULT,
            new FontRequest("Noto Sans CJK", 500));

    public static final FontRequests NOTO_SANS_CJK_BLACK = new FontRequests(
            900,
            FontRequest.DEFAULT,
            new FontRequest("Noto Sans CJK", 900));

    // Noto Serif CJK
    public static final FontRequests NOTO_SERIF_CJK_THIN = new FontRequests(
            100,
            FontRequest.NOTO_SERIF,
            new FontRequest("Noto Serif CJK", 100));

    public static final FontRequests NOTO_SERIF_CJK_LIGHT = new FontRequests(
            300,
            FontRequest.NOTO_SERIF,
            new FontRequest("Noto Serif CJK", 300));

    public static final FontRequests NOTO_SERIF_CJK_REGULAR = new FontRequests(
            400,
            FontRequest.NOTO_SERIF,
            new FontRequest("Noto Serif CJK", 400),
            new FontRequest("Noto Serif CJK", 700));

    public static final FontRequests NOTO_SERIF_CJK_MEDIUM = new FontRequests(
            500,
            FontRequest.NOTO_SERIF,
            new FontRequest("Noto Serif CJK", 500));

    public static final FontRequests NOTO_SERIF_CJK_BLACK = new FontRequests(
            900,
            FontRequest.NOTO_SERIF,
            new FontRequest("Noto Serif CJK", 900));

    public final int weight;
    public final FontRequest[] requests;

    public FontRequests(int weight, FontRequest... requests) {
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
