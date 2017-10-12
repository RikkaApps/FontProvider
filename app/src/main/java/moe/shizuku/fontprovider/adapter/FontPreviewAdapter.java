package moe.shizuku.fontprovider.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Arrays;

import moe.shizuku.fontprovider.font.FontInfo;
import moe.shizuku.fontprovider.viewholder.FontPreviewViewHolder;
import moe.shizuku.support.recyclerview.BaseRecyclerViewAdapter;

/**
 * Created by rikka on 2017/10/3.
 */

public class FontPreviewAdapter extends BaseRecyclerViewAdapter {

    private FontInfo mFontInfo;
    private int mLocaleIndex;
    private Context mContext;

    public FontPreviewAdapter(FontInfo fontInfo, int localeIndex, Context context) {
        super(Arrays.asList(fontInfo.getStyle()));

        getCreatorPool().putRule(FontInfo.Style.class, FontPreviewViewHolder.CREATOR);

        mFontInfo = fontInfo;
        mLocaleIndex = localeIndex;
        mContext = context;
    }

    public FontInfo getFontInfo() {
        return mFontInfo;
    }

    public int getLocaleIndex() {
        return mLocaleIndex;
    }

    @Override
    public LayoutInflater onGetLayoutInflater(View parent) {
        if (mContext != null) {
            return LayoutInflater.from(mContext);
        }
        return super.onGetLayoutInflater(parent);
    }
}
