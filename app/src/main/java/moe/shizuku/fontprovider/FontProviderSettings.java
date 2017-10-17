package moe.shizuku.fontprovider;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by rikka on 2017/10/11.
 */

public class FontProviderSettings {

    public static final String MAX_CACHE = "max_memory_cache";

    private static SharedPreferences sPreferences;

    public static void init(Context context) {
        if (sPreferences == null) {
            sPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        }
    }

    public static int getMaxCache() {
        return sPreferences.getInt(MAX_CACHE, 1024 * 1024 * 100);
    }
}
