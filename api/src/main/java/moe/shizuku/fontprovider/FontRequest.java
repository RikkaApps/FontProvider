package moe.shizuku.fontprovider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;

import java.util.Arrays;

import moe.shizuku.fontprovider.font.Font;
import moe.shizuku.fontprovider.font.FontFamily;

/**
 * Created by rikka on 2017/9/30.
 */
public class FontRequest {

    public static final FontRequest DEFAULT = new FontRequest();

    public static final FontRequest NOTO_SERIF = new FontRequest("Noto Serif", 400, 700);

    public final String name;
    public final int[] weight;

    private FontRequest() {
        this(null, null);
    }

    public FontRequest(String name, int... weight) {
        this.name = name;
        this.weight = weight;
    }

    public FontFamily[] getFontFamily(IFontProvider fontProvider) throws RemoteException {
        return fontProvider.getFontFamily(name, weight);
    }

    public FontFamily[] getFontFamily(ContentResolver resolver) throws RemoteException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < weight.length - 1; i++) {
            sb.append(weight[i]).append(',');
        }
        sb.append(weight[weight.length - 1]);

        Cursor cursor = resolver.query(
                Uri.parse("content://moe.shizuku.fontprovider/font/" + name),
                null, "weight=?", new String[]{sb.toString()}, null);
        if (cursor != null
                && cursor.getCount() > 0) {
            cursor.moveToFirst();
            byte[] bytes = cursor.getBlob(0);

            cursor.close();

            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(bytes, 0, bytes.length);
            parcel.setDataPosition(0);

            FontFamily[] result = parcel.createTypedArray(FontFamily.CREATOR);
            parcel.recycle();
            return result;
        }
        return null;
        //return fontProvider.getFontFamily(name, weight);
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
