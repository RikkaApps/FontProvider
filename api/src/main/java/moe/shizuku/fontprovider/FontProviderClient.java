package moe.shizuku.fontprovider;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public interface Callback {
        /**
         * Called after ServiceConnection#onServiceConnected.
         *
         * @param client FontProviderClient
         * @return true for unbindService automatically, false for keep ServiceConnection.
         */
        boolean onServiceConnected(FontProviderClient client, ServiceConnection serviceConnection);
    }

    /**
     * Add dummy Typefaces if not exist.
     *
     * @param names Family names
     */
    public static void addPlaceholderFamilies(String... names) {
        Map<String, Typeface> typefaceMap = TypefaceCompat.getSystemFontMap();
        if (typefaceMap == null) {
            return;
        }

        for (String name : names) {
            if (!typefaceMap.containsKey(name)) {
                typefaceMap.put(name, TypefaceCompat.createWeightAlias(Typeface.SANS_SERIF, 400));
            }
        }
    }

    /**
     * Create FontProviderClient asynchronously, when call replace, all TextView's Typeface
     * will be replaced automatically if matched (by traversal all TextView).
     *
     * @param activity Activity
     * @param callback Callback
     */
    public static void create(Activity activity, Callback callback) {
        create((Context) activity, callback);
    }

    /**
     * Create FontProviderClient asynchronously.
     *
     * @param context Context
     * @param callback Callback
     */
    public static void create(Context context, Callback callback) {
        Intent intent = new Intent(ACTION)
                .setPackage(PACKAGE);

        sBufferCache.clear();

        try {
            FontProviderServiceConnection connection = new FontProviderServiceConnection(context, callback);
            context.getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.i(TAG, "can't bindService", e);
        }
    }


    /**
     * Create FontProviderClient synchronously (use ContentProvider, slower).
     *
     * @param context Context
     */
    public static FontProviderClient createSync(Context context) {
        context = context.getApplicationContext();

        sBufferCache.clear();

        return new FontProviderClient(context);
    }

    private static Map<String, ByteBuffer> sBufferCache = new HashMap<>();

    private final Context mContext;
    private final ContentResolver mResolver;
    private final IFontProvider mFontProvider;

    private FontProviderClient(Context context) {
        mContext = context;
        mResolver = context.getContentResolver();
        mFontProvider = null;
    }

    FontProviderClient(Context context, IFontProvider fontProvider) {
        mContext = context;
        mResolver = null;
        mFontProvider = fontProvider;
    }

    /**
     * Replace font family with specified font, weight will be resolved by family name.
     *
     * @param name font family name
     * @param fontName font name, such as "Noto Sans CJK"
     * @return Typeface using to replace.
     */
    public Typeface replace(String name, String fontName) {
        return replace(name, FontRequests.create(name, fontName));
    }

    public Typeface replace(String name, FontRequests fontRequests) {
        Typeface typeface = request(fontRequests);
        if (typeface != null
                && TypefaceCompat.getSystemFontMap() != null) {
            if (mContext instanceof Activity) {
                Activity activity = (Activity) mContext;
                View decor = activity.getWindow().getDecorView();
                if (decor instanceof ViewGroup) {
                    replaceTypeface((ViewGroup) decor, name, typeface);
                }
            }

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
                long time = System.currentTimeMillis();

                if (mFontProvider != null) {
                    fontFamilies = FontFamily.combine(fontFamilies, fontRequest.loadFontFamily(mFontProvider));
                } else {
                    fontFamilies = FontFamily.combine(fontFamilies, fontRequest.loadFontFamily(mResolver));
                }

                Log.d(TAG, "get info for "+ fontRequest.name + " costs " + (System.currentTimeMillis() - time) + "ms");
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
                try {
                    if (Build.VERSION.SDK_INT >= 24) {
                        ByteBuffer byteBuffer = font.buffer != null ?
                                font.buffer : sBufferCache.get(font.filename);

                        if (byteBuffer == null) {
                            long time = System.currentTimeMillis();

                            ParcelFileDescriptor pfd = null;
                            int size = (int) font.size;

                            if (mFontProvider != null) {
                                pfd = mFontProvider.getFontFileDescriptor(font.filename);
                            } else {
                                AssetFileDescriptor afd = mResolver.openAssetFileDescriptor(
                                        Uri.parse("content://moe.shizuku.fontprovider/file/" + font.filename), "r");
                                if (afd != null) {
                                    pfd = afd.getParcelFileDescriptor();
                                }
                            }

                            Log.d(TAG, "open file " + font.filename + " costs " + (System.currentTimeMillis() - time) + "ms");

                            if (pfd == null) {
                                Log.e(TAG, "ParcelFileDescriptor is null");
                                return null;
                            }

                            FileInputStream is = new FileInputStream(pfd.getFileDescriptor());
                            FileChannel fileChannel = is.getChannel();
                            byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);

                            sBufferCache.put(font.filename, byteBuffer);
                        }

                        if (!fontFamilyCompat.addFont(byteBuffer, font.ttcIndex, font.weight, font.italic ? 1 : 0)) {
                            return null;
                        }
                    } else {
                        String path = font.path;

                        if (path == null) {
                            Log.e(TAG, "Font " + font.filename + " not downloaded?");
                            return null;
                        }

                        if (!fontFamilyCompat.addFont(path, font.weight, font.italic ? 1 : 0)) {
                            return null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            if (!fontFamilyCompat.freeze()) {
                return null;
            }

            Array.set(families, i++, fontFamilyCompat.getFontFamily());
        }

        return TypefaceCompat.createFromFamiliesWithDefault(families, fontRequests.weight[0], -1);
    }

    private List<TextView> mTextViewCache = new ArrayList<>();

    private void replaceTypeface(ViewGroup parent, String name, Typeface typeface) {
        if (mTextViewCache.isEmpty()) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                View view = parent.getChildAt(i);
                if (view instanceof ViewGroup) {
                    replaceTypeface((ViewGroup) view, name, typeface);
                } else if (view instanceof TextView) {
                    mTextViewCache.add((TextView) view);

                    replaceTypeface((TextView) view, name, typeface);
                }
            }
        } else {
            for (TextView view : mTextViewCache) {
                replaceTypeface(view, name, typeface);
            }
        }
    }

    private void replaceTypeface(TextView view, String name, final Typeface typeface) {
        Typeface[] typefaces = new Typeface[4];
        typefaces[0] = Typeface.create(name, Typeface.NORMAL);
        typefaces[1] = Typeface.create(name, Typeface.BOLD);
        typefaces[2] = Typeface.create(name, Typeface.ITALIC);
        typefaces[3] = Typeface.create(name, Typeface.BOLD_ITALIC);

        for (final Typeface t : typefaces) {
            if (view.getTypeface().equals(t)) {
                view.setTypeface(Typeface.create(typeface, t.getStyle()));
                break;
            }
        }
    }
}
