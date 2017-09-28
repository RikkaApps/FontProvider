package moe.shizuku.fontprovider.api;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import moe.shizuku.fontprovider.IFontProvider;


/**
 * Created by rikka on 2017/9/27.
 */

class FontProviderServiceConnection implements ServiceConnection {

    private static final String TAG = "FontProviderServiceConnection";

    private Context mContext;
    private TypefaceReplacer.FontRequest[] mFontRequests;

    FontProviderServiceConnection(Context context, TypefaceReplacer.FontRequest[] fontRequests) {
        mContext = context;
        mFontRequests = fontRequests;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        IFontProvider fontProvider = IFontProvider.Stub.asInterface(binder);

        for (TypefaceReplacer.FontRequest request : mFontRequests) {
            boolean succeed = TypefaceReplacer.request(fontProvider, request);

            if (succeed) {
                Log.i(TAG, "succeed: " + request.toString());
            } else {
                Log.w(TAG, "failed: " + request.toString());
            }
        }

        unbind();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    void unbind() {
        if (mContext != null) {
            try {
                mContext.unbindService(this);
            } catch (Exception e) {
                e.printStackTrace();
                Log.w(TAG, "failed to unbindService");
            }
        }
    }
}
