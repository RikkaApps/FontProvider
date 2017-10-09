package moe.shizuku.fontprovider.font;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;

/**
 * Created by rikka on 2017/9/30.
 */
public class Font implements Parcelable {

    public final String filename;
    public final ByteBuffer buffer;
    public final int ttcIndex;
    public final int weight;
    public final boolean italic;

    public String path;
    public long size;

    public Font(String filename) {
        this(filename, false);
    }

    public Font(String filename, boolean italic) {
        this(filename, 0, -1, italic);
    }

    public Font(ByteBuffer buffer, int ttcIndex, int weight, boolean italic) {
        this(null, buffer, ttcIndex, weight, italic);
    }

    public Font(String filename, int ttcIndex, int weight, boolean italic) {
        this(filename, null, ttcIndex, weight, italic);
    }

    public Font(String filename, ByteBuffer buffer, int ttcIndex, int weight, boolean italic) {
        this.buffer = buffer;
        this.filename = filename;
        this.ttcIndex = ttcIndex;
        this.weight = weight;
        this.italic = italic;
    }

    @Override
    public String toString() {
        return "Font{" +
                "filename='" + filename + '\'' +
                ", ttcIndex=" + ttcIndex +
                ", weight=" + weight +
                ", italic=" + italic +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.filename);
        dest.writeInt(this.ttcIndex);
        dest.writeInt(this.weight);
        dest.writeByte(this.italic ? (byte) 1 : (byte) 0);
        dest.writeString(this.path);
        dest.writeLong(this.size);
    }

    protected Font(Parcel in) {
        this.filename = in.readString();
        this.buffer = null;
        this.ttcIndex = in.readInt();
        this.weight = in.readInt();
        this.italic = in.readByte() != 0;
        this.path = in.readString();
        this.size = in.readLong();
    }

    public static final Parcelable.Creator<Font> CREATOR = new Parcelable.Creator<Font>() {
        @Override
        public Font createFromParcel(Parcel source) {
            return new Font(source);
        }

        @Override
        public Font[] newArray(int size) {
            return new Font[size];
        }
    };
}
