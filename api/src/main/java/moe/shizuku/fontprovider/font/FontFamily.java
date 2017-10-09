package moe.shizuku.fontprovider.font;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Created by rikka on 2017/9/30.
 */
public class FontFamily implements Parcelable {

    public final int variant;
    public final String language;
    public final Font[] fonts;

    public static FontFamily[] combine(FontFamily first, FontFamily[] array) {
        if (array == null) {
            return new FontFamily[]{first};
        }

        FontFamily[] result = new FontFamily[array.length + 1];
        result[0] = first;
        System.arraycopy(array, 0, result, 1, array.length);
        return result;
    }

    public static FontFamily[] combine(FontFamily[]... arrays) {
        int length = 0;
        for (FontFamily[] array : arrays) {
            if (array == null) {
                continue;
            }
            length += array.length;
        }

        FontFamily[] result = new FontFamily[length];
        length = 0;
        for (FontFamily[] array : arrays) {
            if (array == null) {
                continue;
            }

            System.arraycopy(array, 0, result, length, array.length);
            length += array.length;
        }
        return result;
    }

    public static FontFamily[] createFromTtc(String filename, String[] languages) {
        return createFromTtc(filename, languages, null);
    }

    public static FontFamily[] createFromTtc(String filename, String[] languages, int weight) {
        return createFromTtc(filename, languages, null, weight, 0, false);
    }

    public static FontFamily[] createFromTtc(String filename, String[] languages, int[] ttcIndex) {
        return createFromTtc(filename, languages, ttcIndex, -1, 0, false);
    }

    public static FontFamily[] createFromTtc(String filename, String[] languages, int[] ttcIndex, int weight, int variant, boolean italic) {
        FontFamily[] fontFamilies = new FontFamily[languages.length];
        for (int i = 0; i < languages.length; i++) {
            fontFamilies[i] = new FontFamily(
                    languages[i], variant,
                    new Font(filename, ttcIndex == null ? i : ttcIndex[i], weight, italic));
        }
        return fontFamilies;
    }

    public FontFamily(String language, Font... fonts) {
        this(language, 0, fonts);
    }

    public FontFamily(String language, int variant, Font... fonts) {
        this.language = language;
        this.variant = variant;
        this.fonts = fonts;
    }

    @Override
    public String toString() {
        return "FontFamily{" +
                "variant=" + variant +
                ", language='" + language + '\'' +
                ", fonts=" + Arrays.toString(fonts) +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.variant);
        dest.writeString(this.language);
        dest.writeTypedArray(this.fonts, flags);
    }

    protected FontFamily(Parcel in) {
        this.variant = in.readInt();
        this.language = in.readString();
        this.fonts = in.createTypedArray(Font.CREATOR);
    }

    public static final Parcelable.Creator<FontFamily> CREATOR = new Parcelable.Creator<FontFamily>() {
        @Override
        public FontFamily createFromParcel(Parcel source) {
            return new FontFamily(source);
        }

        @Override
        public FontFamily[] newArray(int size) {
            return new FontFamily[size];
        }
    };
}
