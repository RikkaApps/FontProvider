package moe.shizuku.notocjk.provider.api;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import moe.shizuku.notocjk.provider.IFontProvider;

/**
 * Created by rikka on 2017/9/27.
 */

public class TypefaceReplacer {

    private static final String TAG = "TypefaceReplacer";

    private static final String ACTION = "moe.shizuku.notocjk.provider.FontProviderService.BIND";
    private static final String PACKAGE = "moe.shizuku.notocjk.provider";

    private static TypefaceReplacerImpl sImpl;

    public static void init(Context context, String[] requestFonts) {
        Intent intent = new Intent(ACTION)
                .setPackage(PACKAGE);

        context.bindService(intent, new FontProviderServiceConnection(requestFonts), Context.BIND_AUTO_CREATE);
    }

    static TypefaceReplacerImpl getImpl() {
        if (sImpl != null) {
            return sImpl;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sImpl = new TypefaceReplacerImpl26();
        } else {
            throw new IllegalStateException("unsupported system version");
        }
        return sImpl;
    }

    static void replace(IFontProvider fontProvider, String family) {
        boolean succeed = getImpl().replace(fontProvider, family);
        if (succeed) {
            Log.i(TAG, "succeed to replace family " + family);
        } else {
            Log.w(TAG, "failed to replace family " + family);
        }
    }
}
