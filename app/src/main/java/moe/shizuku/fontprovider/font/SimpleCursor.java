package moe.shizuku.fontprovider.font;

import android.database.AbstractCursor;

/**
 * Created by rikka on 2017/10/9.
 */

public class SimpleCursor extends AbstractCursor {

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public String[] getColumnNames() {
        return new String[0];
    }

    @Override
    public String getString(int column) {
        return null;
    }

    @Override
    public short getShort(int column) {
        return 0;
    }

    @Override
    public int getInt(int column) {
        return 0;
    }

    @Override
    public long getLong(int column) {
        return 0;
    }

    @Override
    public float getFloat(int column) {
        return 0;
    }

    @Override
    public double getDouble(int column) {
        return 0;
    }

    @Override
    public boolean isNull(int column) {
        return false;
    }
}
