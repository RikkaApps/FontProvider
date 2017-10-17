package moe.shizuku.fontprovider.adapter;

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

    public FontPreviewAdapter(FontInfo fontInfo) {
        super(Arrays.asList(fontInfo.getStyle()));

        getCreatorPool().putRule(FontInfo.Style.class, FontPreviewViewHolder.CREATOR);

        mFontInfo = fontInfo;
    }

    public FontInfo getFontInfo() {
        return mFontInfo;
    }

    public int getLocaleIndex() {
        return mLocaleIndex;
    }

    public void setLocaleIndex(int localeIndex) {
        mLocaleIndex = localeIndex;
    }
}
