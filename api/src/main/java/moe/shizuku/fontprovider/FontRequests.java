package moe.shizuku.fontprovider;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import moe.shizuku.fontprovider.font.BundledFontFamily;

/**
 * Created by rikka on 2017/10/1.
 */

public class FontRequests implements Parcelable {

    private static FontRequest[] filterDefault(@NonNull FontRequest[] requests) {
        List<FontRequest> list = new ArrayList<>();
        for (FontRequest request : requests) {
            if (request.equals(FontRequest.DEFAULT)) {
                continue;
            }
            list.add(request);
        }
        return list.toArray(new FontRequest[list.size()]);
    }

    public boolean ignoreDefault() {
        for (FontRequest fontRequest : requests) {
            if (fontRequest.equals(FontRequest.DEFAULT)) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public BundledFontFamily request(@NonNull ContentResolver resolver) {
        Bundle data = new Bundle();
        data.setClassLoader(this.getClass().getClassLoader());
        data.putParcelable("data", this);

        Bundle result = resolver.call(
                Uri.parse("content://moe.shizuku.fontprovider"), "request", "bundled", data);
        if (result != null) {
            result.setClassLoader(this.getClass().getClassLoader());
            return result.getParcelable("data");
        }
        return null;
    }

    public static FontRequest[] DEFAULT_SANS_SERIF_FONTS = new FontRequest[]{FontRequest.DEFAULT};

    public static FontRequest[] DEFAULT_SERIF_FONTS = new FontRequest[]{FontRequest.NOTO_SERIF};

    private static FontRequest[] getDefaultFont(boolean serif) {
        return serif ? DEFAULT_SERIF_FONTS : DEFAULT_SANS_SERIF_FONTS;
    }

    public static FontRequests create(FontRequest[] defaultFonts, String fontName, int... weight) {
        return new FontRequests(weight,
                FontRequest.combine(defaultFonts, new FontRequest[]{new FontRequest(fontName, weight)})
        );
    }

    public final int[] weight;
    public final FontRequest[] requests;

    public FontRequests(int weight, FontRequest... requests) {
        this(new int[]{weight}, requests);
    }

    public FontRequests(int[] weight, FontRequest... requests) {
        this.weight = weight;
        this.requests = requests;
    }

    @Override
    public String toString() {
        return "FontRequests{" +
                "weight=" + weight +
                ", requests=" + Arrays.toString(requests) +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(this.weight);
        dest.writeTypedArray(filterDefault(this.requests), flags);
    }

    protected FontRequests(Parcel in) {
        this.weight = in.createIntArray();
        this.requests = in.createTypedArray(FontRequest.CREATOR);
    }

    public static final Parcelable.Creator<FontRequests> CREATOR = new Parcelable.Creator<FontRequests>() {
        @Override
        public FontRequests createFromParcel(Parcel source) {
            return new FontRequests(source);
        }

        @Override
        public FontRequests[] newArray(int size) {
            return new FontRequests[size];
        }
    };
}
