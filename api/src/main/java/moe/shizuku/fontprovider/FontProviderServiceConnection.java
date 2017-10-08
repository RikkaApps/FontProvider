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
    private FontProviderClient.Callback mCallback;

    FontProviderServiceConnection(Context context, FontProviderClient.Callback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        IFontProvider fontProvider = IFontProvider.Stub.asInterface(binder);

        FontProviderClient client = new FontProviderClient(this, fontProvider);
        if (mCallback.onServiceConnected(client)) {
            client.unbindService(mContext);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}
