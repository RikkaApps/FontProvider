package moe.shizuku.fontprovider;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;
import moe.shizuku.fontprovider.font.FontManager;

/**
 * Created by rikka on 2017/10/3.
 */

public class FontProviderApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        Fabric.with(this, crashlyticsKit);

        FontProviderSettings.init(this);
        FontManager.init(this);
    }
}
