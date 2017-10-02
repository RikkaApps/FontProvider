package moe.shizuku.fontprovider;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by rikka on 2017/9/29.
 */

public class FontInfo implements Parcelable {

    private final String name;
    private final String variant;
    private final String[] language;
    private final int[] index;
    private final String size;
    private final String[] preview_text;
    private final String url_prefix;
    private final Style[] style;

    private final Typeface[] typefaces;

    public String getName() {
        return name;
    }

    public int getVariant() {
        if ("compact".equals(variant)) {
            return 1;
        } else if ("elegant".equals(variant)) {
            return 2;
        }
        return 0;
    }

    public String[] getLanguage() {
        return language;
    }

    public int[] getIndex() {
        return index;
    }

    public String getSize() {
        return size;
    }

    public String getUrlPrefix() {
        return url_prefix;
    }

    public String[] getPreviewText() {
        return preview_text;
    }

    public Style[] getStyle() {
        return style;
    }

    public Style[] getStyle(int... weight) {
        List<Style> styles = new ArrayList<>();
        for (int w : weight) {
            for (Style s : style) {
                if (w == s.getWeight()) {
                    styles.add(s);
                }
            }
        }

        if (styles.isEmpty()
                && weight.length != 1 && weight[0] != 400) {
            return getStyle(400);
        }

        return styles.toArray(new Style[styles.size()]);
    }

    public Typeface getTypeface(int style) {
        return typefaces[style];
    }

    public void setTypeface(Typeface typeface, int style) {
        typefaces[style] = typeface;
    }

    public static class Style implements Parcelable {

        private int weight;
        private boolean italic;
        private String name;
        private String ttc;
        private String[] ttf;

        public String getName() {
            return name;
        }

        public int getWeight() {
            return weight;
        }

        public boolean isItalic() {
            return italic;
        }

        public String getTtc() {
            return ttc;
        }

        public String[] getTtf() {
            return ttf;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public String toString() {
            return "Style{" +
                    "weight=" + weight +
                    ", italic=" + italic +
                    ", ttc='" + ttc + '\'' +
                    ", ttf=" + Arrays.toString(ttf) +
                    '}';
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.name);
            dest.writeInt(this.weight);
            dest.writeByte(this.italic ? (byte) 1 : (byte) 0);
            dest.writeString(this.ttc);
            dest.writeStringArray(this.ttf);
        }

        Style(Parcel in) {
            this.name = in.readString();
            this.weight = in.readInt();
            this.italic = in.readByte() != 0;
            this.ttc = in.readString();
            this.ttf = in.createStringArray();
        }

        public static final Parcelable.Creator<Style> CREATOR = new Parcelable.Creator<Style>() {
            @Override
            public Style createFromParcel(Parcel source) {
                return new Style(source);
            }

            @Override
            public Style[] newArray(int size) {
                return new Style[size];
            }
        };
    }

    @Override
    public String toString() {
        return "FontInfo{" +
                "name='" + name + '\'' +
                ", variant='" + variant + '\'' +
                ", language=" + Arrays.toString(language) +
                ", index=" + Arrays.toString(index) +
                ", style=" + Arrays.toString(style) +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.variant);
        dest.writeStringArray(this.language);
        dest.writeIntArray(this.index);
        dest.writeString(this.size);
        dest.writeStringArray(this.preview_text);
        dest.writeString(this.url_prefix);
        dest.writeTypedArray(this.style, flags);
    }

    private FontInfo(Parcel in) {
        this.name = in.readString();
        this.variant = in.readString();
        this.language = in.createStringArray();
        this.index = in.createIntArray();
        this.size = in.readString();
        this.preview_text = in.createStringArray();
        this.url_prefix = in.readString();
        this.style = in.createTypedArray(Style.CREATOR);
        this.typefaces = new Typeface[style.length];
    }

    public static final Parcelable.Creator<FontInfo> CREATOR = new Parcelable.Creator<FontInfo>() {
        @Override
        public FontInfo createFromParcel(Parcel source) {
            return new FontInfo(source);
        }

        @Override
        public FontInfo[] newArray(int size) {
            return new FontInfo[size];
        }
    };
}
