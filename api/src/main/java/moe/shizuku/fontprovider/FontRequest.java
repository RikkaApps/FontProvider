package moe.shizuku.fontprovider;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Created by rikka on 2017/9/30.
 */
public class FontRequest implements Parcelable {

    /** empty FontRequest that tell FontProviderClient to use first font of system fallback list **/
    public static final FontRequest DEFAULT = new FontRequest();

    /** Noto Color Emoji on Oreo **/
    public static final FontRequest NOTO_COLOR_EMOJI = new FontRequest("Noto Color Emoji", 1);

    /** Noto Color Emoji on Nougat **/
    public static final FontRequest NOTO_COLOR_EMOJI_NOUGAT = new FontRequest("Noto Color Emoji", 2);

    /** Noto Serif **/
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

    @Override
    public String toString() {
        return "FontRequest{" +
                "name='" + name + '\'' +
                ", weight=" + Arrays.toString(weight) +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeIntArray(this.weight);
    }

    protected FontRequest(Parcel in) {
        this.name = in.readString();
        this.weight = in.createIntArray();
    }

    public static final Parcelable.Creator<FontRequest> CREATOR = new Parcelable.Creator<FontRequest>() {
        @Override
        public FontRequest createFromParcel(Parcel source) {
            return new FontRequest(source);
        }

        @Override
        public FontRequest[] newArray(int size) {
            return new FontRequest[size];
        }
    };
}
