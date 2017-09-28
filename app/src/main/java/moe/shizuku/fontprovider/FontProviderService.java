package moe.shizuku.fontprovider;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.util.HashMap;
import java.util.Map;

import moe.shizuku.fontprovider.utils.ContextUtils;
import moe.shizuku.fontprovider.utils.MemoryFileUtils;
import moe.shizuku.fontprovider.utils.ParcelFileDescriptorUtils;

/**
 * Created by rikka on 2017/9/27.
 */

public class FontProviderService extends Service {

    private static final String TAG = "FontProviderService";

    private IFontProviderBinder mBinder;

    private static Map<String, MemoryFile> sCache;
    private static final Map<String, Integer> sFileSize;

    static {
        sCache = new HashMap<>();

        sFileSize = BuildConfig.BUILT_IN_FONTS_SIZE;
    }

    public static void closeAll() {
        for (Map.Entry<String, MemoryFile> entry: sCache.entrySet()) {
            entry.getValue().close();
        }

        sCache.clear();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mBinder = new IFontProviderBinder(this);
    }

    private ParcelFileDescriptor getParcelFileDescriptor(String fileName) {
        MemoryFile mf = sCache.get(fileName);
        if (mf != null) {
            Log.i(TAG, "MemoryFile " + fileName + " is in the cache");
            return ParcelFileDescriptorUtils.dupSilently(getFileDescriptor(mf));
        }

        long time = System.currentTimeMillis();

        Log.i(TAG, "loading file " + fileName);

        if (sFileSize.containsKey(fileName)) {
            mf = MemoryFileUtils.fromAsset(getAssets(), fileName, sFileSize.get(fileName));
        }

        if (mf == null) {
            File file = ContextUtils.getFile(this, fileName);
            if (file.exists()) {
                mf = MemoryFileUtils.fromFile(file);
                if (mf != null) {
                    sFileSize.put(fileName, mf.length());
                }
            }
        }

        if (mf == null) {
            Log.w(TAG, "loading " + fileName + " failed");
            return null;
        }

        Log.i(TAG, "loading finished in " + (System.currentTimeMillis() - time) + "ms");
        sCache.put(fileName, mf);

        return ParcelFileDescriptorUtils.dupSilently(getFileDescriptor(mf));
    }

    private FileDescriptor getFileDescriptor(MemoryFile mf) {
        FileDescriptor fd = MemoryFileUtils.getFileDescriptor(mf);
        if (fd == null) {
            Log.w(TAG, "can't get FileDescriptor from MemoryFile");
            return null;
        }
        return fd;
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
            return getParcelFileDescriptor(filename);
        }

        @Override
        public int getFontFileSize(String filename) throws RemoteException {
            if (sFileSize.containsKey(filename)) {
                return sFileSize.get(filename);
            }

            File file = ContextUtils.getFile(mContext, filename);
            if (file.exists()) {
                return (int) file.length();
            }
            return 0;
        }
    }
}
