package moe.shizuku.fontprovider.font;

import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rikka on 2017/9/29.
 */

@Keep
public class FontInfo implements Parcelable {

    private final String name;
    private final String variant;
    private final String[] language;
    private final int[] ttc_index;
    private final String size;
    private final String[] preview_text;
    private final String url_prefix;
    private final Style[] style;

    private static final Map<String, Typeface> sCache = new HashMap<>();

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

    public int[] getTtcIndex() {
        return ttc_index;
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
        if (weight.length == 0) {
            return getStyle();
        }

        for (int i = 0; i < weight.length; i++) {
            boolean match = false;
            for (Style s : style) {
                if (weight[i] == s.getWeight()) {
                    match = true;
                    break;
                }
            }

            if (!match) {
                int bestWeight = weight[i];

                int diff = Integer.MAX_VALUE;
                for (Style s : style) {
                    if (Math.abs(weight[i] - s.weight) < diff) {
                        diff = Math.abs(weight[i] - s.weight);
                        bestWeight = s.weight;
                    }
                }

                weight[i] = bestWeight;
            }
        }

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
        return sCache.get(name  + style);
    }

    public void setTypeface(Typeface typeface, int style) {
        sCache.put(name + style, typeface);
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
                ", ttc_index=" + Arrays.toString(ttc_index) +
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
        dest.writeIntArray(this.ttc_index);
        dest.writeString(this.size);
        dest.writeStringArray(this.preview_text);
        dest.writeString(this.url_prefix);
        dest.writeTypedArray(this.style, flags);
    }

    private FontInfo(Parcel in) {
        this.name = in.readString();
        this.variant = in.readString();
        this.language = in.createStringArray();
        this.ttc_index = in.createIntArray();
        this.size = in.readString();
        this.preview_text = in.createStringArray();
        this.url_prefix = in.readString();
        this.style = in.createTypedArray(Style.CREATOR);
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
