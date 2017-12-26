package moe.shizuku.fontprovider.font;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SharedMemory;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import java.io.File;

import moe.shizuku.fontprovider.FontRequests;
import moe.shizuku.fontprovider.IFontProvider;
import moe.shizuku.support.utils.ContextUtils;

/**
 * Created by rikka on 2017/9/27.
 */

public class FontProviderService extends Service {

    private static final String TAG = "FontProviderService";

    private IFontProviderBinder mBinder;

    @Override
    public void onCreate() {
        super.onCreate();

        mBinder = new IFontProviderBinder(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class IFontProviderBinder extends IFontProvider.Stub {

        private Context mContext;

        public IFontProviderBinder(Context context) {
            mContext = context;
        }

        @Override
        public ParcelFileDescriptor getFontFileDescriptor(String filename) throws RemoteException {
            return FontManager.getParcelFileDescriptor(mContext, filename);
        }

        @RequiresApi(api = Build.VERSION_CODES.O_MR1)
        @Override
        public SharedMemory getFontSharedMemory(String filename) throws RemoteException {
            return FontManager.getSharedMemory(mContext, filename);
        }

        @Deprecated
        @Override
        public int getFontFileSize(String filename) throws RemoteException {
            return FontManager.getFileSize(mContext, filename);
        }

        @Override
        public FontFamily[] getFontFamily(String name, int[] weight) throws RemoteException {
            return FontManager.getFontFamily(mContext, name, weight);
        }

        @Deprecated
        @Override
        public String getFontFilePath(String filename) throws RemoteException {
            File file = ContextUtils.getExternalFile(mContext, filename);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
            return null;
        }

        @Override
        public BundledFontFamily getBundledFontFamily(FontRequests requests) throws RemoteException {
            return FontManager.getBundledFontFamily(mContext, requests);
        }
    }
}
