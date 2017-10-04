package moe.shizuku.fontprovider.viewholder;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import moe.shizuku.fontprovider.FontInfo;
import moe.shizuku.fontprovider.FontProviderClient;
import moe.shizuku.fontprovider.FontRequest;
import moe.shizuku.fontprovider.FontRequests;
import moe.shizuku.fontprovider.R;
import moe.shizuku.fontprovider.adapter.FontPreviewAdapter;
import moe.shizuku.utils.recyclerview.BaseViewHolder;

/**
 * Created by rikka on 2017/10/3.
 */

public class FontPreviewViewHolder extends BaseViewHolder<FontInfo.Style> {

    public static final Creator<FontInfo.Style> CREATOR = new Creator<FontInfo.Style>() {
        @Override
        public BaseViewHolder<FontInfo.Style> createViewHolder(LayoutInflater inflater, ViewGroup parent) {
            return new FontPreviewViewHolder(inflater.inflate(R.layout.item_font_preview, parent, false));
        }
    };

    private TextView title;
    private TextView text;

    public FontPreviewViewHolder(View itemView) {
        super(itemView);

        title = itemView.findViewById(android.R.id.title);
        text = itemView.findViewById(android.R.id.text1);

        setIsRecyclable(false);
    }

    @Override
    public FontPreviewAdapter getAdapter() {
        return (FontPreviewAdapter) super.getAdapter();
    }

    @Override
    public void onBind() {
        Context context = itemView.getContext();

        final FontInfo font = getAdapter().getFontInfo();
        final FontInfo.Style style = getData();
        final int localeIndex = getAdapter().getLocaleIndex();

        title.setText(style.getName());
        text.setText(font.getPreviewText()[localeIndex]);

        Typeface typeface = font.getTypeface(getAdapterPosition());
        if (typeface == null) {
            FontProviderClient.create(context, new FontProviderClient.Callback() {
                @Override
                public boolean onServiceConnected(FontProviderClient client) {
                    Typeface typeface;

                    if (font.getLanguage()[localeIndex] == null) {
                        typeface = client.request(new FontRequests(
                                style.getWeight(),
                                new FontRequest(font.getName(), style.getWeight())
                        ));
                    } else {
                        typeface = client.request(new FontRequests(

                                style.getWeight(),
                                FontRequest.DEFAULT,
                                new FontRequest(font.getName(), style.getWeight())
                        ));
                    }

                    text.setTypeface(typeface);

                    font.setTypeface(typeface, getAdapterPosition());
                    return true;
                }
            });
        } else {
            text.setTypeface(typeface);
        }
    }
}
