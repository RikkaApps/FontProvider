package moe.shizuku.fontprovider;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;


/**
 * Created by rikka on 2017/9/27.
 */

class FontProviderServiceConnection implements ServiceConnection {

    private Context mContext;
    private FontProviderClient.ServiceCallback mCallback;

    FontProviderServiceConnection(Context context, FontProviderClient.ServiceCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        IFontProvider fontProvider = IFontProvider.Stub.asInterface(binder);

        mCallback.onServiceConnected(new FontProviderClient(mContext, fontProvider, this));
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}
