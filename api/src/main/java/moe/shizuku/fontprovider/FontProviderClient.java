package moe.shizuku.fontprovider;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import moe.shizuku.fontprovider.compat.FontFamilyCompat;
import moe.shizuku.fontprovider.compat.TypefaceCompat;
import moe.shizuku.fontprovider.font.Font;
import moe.shizuku.fontprovider.font.FontFamily;

/**
 * Created by rikka on 2017/9/27.
 */

public class FontProviderClient {

    private static final String TAG = "FontProviderClient";

    private static final String ACTION = "moe.shizuku.fontprovider.action.BIND";
    private static final String PACKAGE = "moe.shizuku.fontprovider";

    public interface ServiceCallback {
        void onServiceConnected(FontProviderClient client);
    }

    public static void create(Context context, ServiceCallback callback) {
        context = context.getApplicationContext();

        Intent intent = new Intent(ACTION)
                .setPackage(PACKAGE);

        sBufferCache.clear();

        try {
            context.bindService(intent, new FontProviderServiceConnection(context, callback), Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.i(TAG, "can't bindService", e);
        }
    }

    private static Map<String, ByteBuffer> sBufferCache = new HashMap<>();

    private Context mContext;
    private IFontProvider mFontProvider;
    private ServiceConnection mServiceConnection;

    public FontProviderClient(Context context, IFontProvider fontProvider, ServiceConnection serviceConnection) {
        mContext = context;
        mFontProvider = fontProvider;
        mServiceConnection = serviceConnection;
    }

    public void unbindService() {
        try {
            mContext.unbindService(mServiceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Typeface replace(String name, FontRequests fontRequests) {
        Typeface typeface = request(fontRequests);
        if (typeface != null
                && TypefaceCompat.getSystemFontMap() != null) {
            TypefaceCompat.getSystemFontMap().put(name, typeface);
            return typeface;
        } else {
            return null;
        }
    }

    public Typeface request(FontRequests fontRequests) {
        boolean ignoreDefault = true;
        FontFamily[] fontFamilies = new FontFamily[0];

        for (FontRequest fontRequest : fontRequests.requests) {
            if (fontRequest.equals(FontRequest.DEFAULT)) {
                ignoreDefault = false;
                continue;
            }
            try {
                fontFamilies = FontFamily.combine(fontFamilies, fontRequest.getFontFamily(mFontProvider));
            } catch (RemoteException e) {
                e.printStackTrace();
                return null;
            }
        }

        Object families;
        if (!ignoreDefault) {
            families = Array.newInstance(FontFamilyCompat.getFontFamilyClass(), fontFamilies.length + 1);
        } else {
            families = Array.newInstance(FontFamilyCompat.getFontFamilyClass(), fontFamilies.length);
        }

        int i = 0;
        Object fallbackFonts = TypefaceCompat.getFallbackFontsArray();
        if (fallbackFonts == null
                || Array.getLength(fallbackFonts) == 0) {
            return null;
        }

        if (!ignoreDefault) {
            Array.set(families, i++, Array.get(fallbackFonts, 0));
        }

        for (FontFamily fontFamily : fontFamilies) {
            FontFamilyCompat fontFamilyCompat = new FontFamilyCompat(fontFamily.language, fontFamily.variant);
            if (fontFamilyCompat.getFontFamily() == null) {
                return null;
            }

            for (Font font : fontFamily.fonts) {
                ByteBuffer byteBuffer = font.buffer;
                if (byteBuffer == null) {
                    byteBuffer = sBufferCache.get(font.filename);
                }

                if (byteBuffer == null) {
                    if (font.filename == null) {
                        return null;
                    }

                    try {
                        ParcelFileDescriptor pfd = mFontProvider.getFontFileDescriptor(font.filename);
                        if (pfd == null) {
                            Log.w(TAG, "ParcelFileDescriptor is null");
                            return null;
                        }
                        int size = mFontProvider.getFontFileSize(font.filename);

                        FileInputStream is = new FileInputStream(pfd.getFileDescriptor());
                        FileChannel fileChannel = is.getChannel();
                        byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                if (byteBuffer == null) {
                    return null;
                }

                if (!fontFamilyCompat.addFont(byteBuffer, font.ttcIndex, font.weight, font.italic ? 1 : 0)) {
                    return null;
                }
            }

            if (!fontFamilyCompat.freeze()) {
                return null;
            }

            Array.set(families, i++, fontFamilyCompat.getFontFamily());
        }

        return TypefaceCompat.createFromFamiliesWithDefault(families, fontRequests.weight, -1);
    }
}
