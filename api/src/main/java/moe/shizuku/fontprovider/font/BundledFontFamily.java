package moe.shizuku.fontprovider.font;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rikka on 2017/10/30.
 */

public class BundledFontFamily implements Parcelable {

    public final FontFamily[] families;
    public final Map<String, ParcelFileDescriptor> fd;

    public BundledFontFamily(FontFamily[] families, Map<String, ParcelFileDescriptor> fd) {
        this.families = families;
        this.fd = fd;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(this.families, flags);
        dest.writeInt(this.fd.size());
        for (Map.Entry<String, ParcelFileDescriptor> entry : this.fd.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeParcelable(entry.getValue(), flags);
        }
    }

    protected BundledFontFamily(Parcel in) {
        this.families = in.createTypedArray(FontFamily.CREATOR);
        int fdSize = in.readInt();
        this.fd = new HashMap<>(fdSize);
        for (int i = 0; i < fdSize; i++) {
            String key = in.readString();
            ParcelFileDescriptor value = in.readParcelable(ParcelFileDescriptor.class.getClassLoader());
            this.fd.put(key, value);
        }
    }

    public static final Creator<BundledFontFamily> CREATOR = new Creator<BundledFontFamily>() {
        @Override
        public BundledFontFamily createFromParcel(Parcel source) {
            return new BundledFontFamily(source);
        }

        @Override
        public BundledFontFamily[] newArray(int size) {
            return new BundledFontFamily[size];
        }
    };
}
