package moe.shizuku.fontprovider;

import android.content.Context;

import moe.shizuku.support.utils.Settings;

/**
 * Created by rikka on 2017/10/11.
 */

public class FontProviderSettings {

    public static final String MAX_CACHE = "max_memory_cache";

    public static void init(Context context) {
        Settings.init(context);
    }

    public static int getMaxCache() {
        return Settings.getInt(MAX_CACHE, 1024 * 1024 * 100);
    }
}
