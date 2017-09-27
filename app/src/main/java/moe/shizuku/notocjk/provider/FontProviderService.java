package moe.shizuku.notocjk.provider;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.FileDescriptor;
import java.util.HashMap;
import java.util.Map;

import moe.shizuku.notocjk.provider.utils.AssetUtils;
import moe.shizuku.notocjk.provider.utils.MemoryFileUtils;
import moe.shizuku.notocjk.provider.utils.ParcelFileDescriptorUtils;

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

        sFileSize = new HashMap<>();

        sFileSize.put("NotoSansCJK-Light.ttc",      BuildConfig.NotoSansCJK_Light_Size);
        sFileSize.put("NotoSansCJK-Medium.ttc",     BuildConfig.NotoSansCJK_Medium_Size);

        sFileSize.put("NotoSerifCJK-Light.ttc",     BuildConfig.NotoSerifCJK_Light_Size);
        sFileSize.put("NotoSerifCJK-Regular.ttc",   BuildConfig.NotoSerifCJK_Regular_Size);
        sFileSize.put("NotoSerifCJK-Medium.ttc",    BuildConfig.NotoSerifCJK_Medium_Size);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mBinder = new IFontProviderBinder();
    }

    private ParcelFileDescriptor getParcelFileDescriptor(String fileName) {
        MemoryFile mf = sCache.get(fileName);
        if (mf != null) {
            Log.i(TAG, "MemoryFile " + fileName + " is in the cache");
            return ParcelFileDescriptorUtils.dupSilently(getFileDescriptor(mf));
        }

        long time = System.currentTimeMillis();

        Log.i(TAG, "loading file " + fileName);
        mf = AssetUtils.toMemoryFile(this, fileName, sFileSize.get(fileName));
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

        @Override
        public ParcelFileDescriptor getFontFileDescriptor(String filename) throws RemoteException {
            return getParcelFileDescriptor(filename);
        }

        @Override
        public int getFontFileSize(String filename) throws RemoteException {
            return sFileSize.get(filename);
        }
    }
}
