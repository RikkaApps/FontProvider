package moe.shizuku.fontprovider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import moe.shizuku.fontprovider.font.Font;
import moe.shizuku.fontprovider.font.FontFamily;

/**
 * Created by rikka on 2017/9/30.
 */
public class FontRequest {

    public static final FontRequest DEFAULT = new FontRequest();

    public static final FontRequest NOTO_COLOR_EMOJI = new FontRequest("Noto Color Emoji", 1);
    public static final FontRequest NOTO_COLOR_EMOJI_NOUGAT = new FontRequest("Noto Color Emoji", 2);
    public static final FontRequest NOTO_SERIF = new FontRequest("Noto Serif", 400, 700);

    public static FontRequest[] combine(FontRequest[]... arrays) {
        int length = 0;
        for (FontRequest[] array : arrays) {
            if (array == null) {
                continue;
            }
            length += array.length;
        }

        FontRequest[] result = new FontRequest[length];
        length = 0;
        for (FontRequest[] array : arrays) {
            if (array == null) {
                continue;
            }

            System.arraycopy(array, 0, result, length, array.length);
            length += array.length;
        }
        return result;
    }

    public final String name;
    public final int[] weight;

    private FontRequest() {
        this(null, (int[]) null);
    }

    public FontRequest(String name, int... weight) {
        this.name = name;
        this.weight = weight;
    }

    public FontFamily[] loadFontFamily(IFontProvider fontProvider) throws RemoteException {
        return fontProvider.getFontFamily(name, weight);
    }

    public FontFamily[] loadFontFamily(ContentResolver resolver) throws RemoteException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < weight.length - 1; i++) {
            sb.append(weight[i]).append(',');
        }
        sb.append(weight[weight.length - 1]);

        Cursor cursor = resolver.query(
                Uri.parse("content://moe.shizuku.fontprovider/font/" + name),
                null, "weight=?s", new String[]{sb.toString()}, null);
        if (cursor != null) {
            Bundle bundle = cursor.getExtras();
            bundle.setClassLoader(FontFamily.CREATOR.getClass().getClassLoader());

            Parcelable[] parcelables = bundle.getParcelableArray("data");
            if (parcelables != null) {
                FontFamily[] families = new FontFamily[parcelables.length];
                for (int i = 0; i < parcelables.length; i++) {
                    families[i] = (FontFamily) parcelables[i];
                }
                return families;
            }
        }
        return new FontFamily[0];
    }

    /**/

    @Override
    public String toString() {
        return "FontRequest{" +
                "name='" + name + '\'' +
                ", weight=" + Arrays.toString(weight) +
                '}';
    }
}
