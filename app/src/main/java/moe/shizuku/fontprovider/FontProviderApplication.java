package moe.shizuku.fontprovider;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;
import moe.shizuku.fontprovider.font.FontManager;

/**
 * Created by rikka on 2017/10/3.
 */

public class FontProviderApplication extends Application {

    private static boolean sInitialized;

    public static void init(Context context) {
        if (sInitialized) {
            return;
        }

        sInitialized = true;

        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        Fabric.with(context, crashlyticsKit);

        FontProviderSettings.init(context);
        FontManager.init(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        init(this);
    }
}
