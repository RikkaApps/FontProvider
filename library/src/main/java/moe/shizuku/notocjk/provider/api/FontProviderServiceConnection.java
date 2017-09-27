package moe.shizuku.notocjk.provider.api;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import moe.shizuku.notocjk.provider.IFontProvider;

/**
 * Created by rikka on 2017/9/27.
 */

public class FontProviderServiceConnection implements ServiceConnection {

    private String[] mRequestFonts;

    public FontProviderServiceConnection(String[] requestFonts) {
        mRequestFonts = requestFonts;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        IFontProvider fontProvider = IFontProvider.Stub.asInterface(binder);

        for (String font : mRequestFonts) {
            TypefaceReplacer.replace(fontProvider, font);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}
