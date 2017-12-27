package moe.shizuku.fontprovider.font;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.SharedMemory;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;


public class BundledFontFamily implements Parcelable {

    public final @NonNull FontFamily[] families;
    public final @NonNull Map<String, ParcelFileDescriptor> fd;
    public final @NonNull Map<String, SharedMemory> sm;

    public BundledFontFamily(@NonNull FontFamily[] families, @NonNull Map<String, ParcelFileDescriptor> fd, @NonNull Map<String, SharedMemory> sm) {
        this.families = families;
        this.fd = fd;
        this.sm = sm;
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
        dest.writeInt(this.sm.size());
        for (Map.Entry<String, SharedMemory> entry : this.sm.entrySet()) {
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

        int smSize = in.readInt();
        this.sm = new HashMap<>(smSize);
        for (int i = 0; i < smSize; i++) {
            String key = in.readString();
            SharedMemory value = in.readParcelable(SharedMemory.class.getClassLoader());
            this.sm.put(key, value);
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
