package moe.shizuku.fontprovider;

import java.util.Arrays;

/**
 * Created by rikka on 2017/10/1.
 */

public class FontRequests {

    public static FontRequests create(int weight, FontRequest defaultFont, String name) {
        return new FontRequests(weight, defaultFont, new FontRequest(name, weight));
    }

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
