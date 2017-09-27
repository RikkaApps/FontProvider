package moe.shizuku.notocjk.provider.api;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import moe.shizuku.notocjk.provider.IFontProvider;

/**
 * Created by rikka on 2017/9/27.
 */

public class FontProviderServiceConnection implements ServiceConnection {

    private static final String TAG = "TypefaceReplacer";

    private String[] mRequestFamilies;

    FontProviderServiceConnection(String[] requestFamilies) {
        mRequestFamilies = requestFamilies;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        IFontProvider fontProvider = IFontProvider.Stub.asInterface(binder);

        for (String family : mRequestFamilies) {
            boolean succeed = TypefaceReplacer.replace(fontProvider, family);

            if (succeed) {
                Log.i(TAG, "succeed to replace family " + family);
            } else {
                Log.w(TAG, "failed to replace family " + family);
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}
