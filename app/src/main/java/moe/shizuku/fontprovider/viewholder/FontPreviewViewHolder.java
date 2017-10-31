package moe.shizuku.fontprovider.viewholder;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import moe.shizuku.fontprovider.compat.TypefaceCompat;
import moe.shizuku.fontprovider.font.Font;
import moe.shizuku.fontprovider.font.FontInfo;
import moe.shizuku.fontprovider.FontProviderClient;
import moe.shizuku.fontprovider.FontRequest;
import moe.shizuku.fontprovider.FontRequests;
import moe.shizuku.fontprovider.R;
import moe.shizuku.fontprovider.adapter.FontPreviewAdapter;
import moe.shizuku.support.recyclerview.BaseViewHolder;

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

    private class RequestTask extends AsyncTask<Context, Void, Typeface> {

        private FontInfo mFont;
        private FontInfo.Style mStyle;
        private int mLocaleIndex;

        public RequestTask(FontInfo font, FontInfo.Style style, int localeIndex) {
            mFont = font;
            mStyle = style;
            mLocaleIndex = localeIndex;
        }

        @Override
        protected Typeface doInBackground(Context... contexts) {
            if (sFontProviderClient == null) {
                sFontProviderClient = FontProviderClient.create(contexts[0]);
            }

            Typeface typeface;
            if (mFont.getLanguage()[mLocaleIndex] == null) {
                typeface = sFontProviderClient.request(new FontRequests(
                        new int[]{mStyle.getWeight()},
                        new FontRequest(mFont.getName(), mStyle.getWeight())
                ));
            } else {
                typeface = sFontProviderClient.request(new FontRequests(
                        new int[]{mStyle.getWeight()},
                        FontRequest.DEFAULT,
                        new FontRequest(mFont.getName(), mStyle.getWeight())
                ));
            }

            if (typeface != null) {
                if (mStyle.getWeight() != 400) {
                    typeface = TypefaceCompat.createWeightAlias(typeface, mStyle.getWeight());
                }

                mFont.setTypeface(typeface, getAdapterPosition());
            }
            return typeface;
        }

        @Override
        protected void onPostExecute(Typeface typeface) {
            if (mStyle.isItalic()) {
                text.setTypeface(Typeface.create(typeface, Typeface.ITALIC));
            } else {
                text.setTypeface(typeface);
            }
        }
    }

    private static FontProviderClient sFontProviderClient;

    private TextView title;
    private TextView text;

    private RequestTask mTask;

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

        if (!TextUtils.isEmpty(font.getLanguage()[localeIndex])) {
            text.setTextLocale(Locale.forLanguageTag(font.getLanguage()[localeIndex]));
        }

        Typeface typeface = font.getTypeface(getAdapterPosition());
        if (typeface == null) {
            if (mTask == null) {
                mTask = new RequestTask(font, style, localeIndex);
                mTask.execute(context);
            }
        } else {
            if (style.isItalic()) {
                text.setTypeface(Typeface.create(typeface, Typeface.ITALIC));
            } else {
                text.setTypeface(typeface);
            }
        }
    }

    @Override
    public void onBind(@NonNull List<Object> payloads) {
        final FontInfo font = getAdapter().getFontInfo();
        final int localeIndex = getAdapter().getLocaleIndex();

        text.setText(font.getPreviewText()[localeIndex]);

        if (!TextUtils.isEmpty(font.getLanguage()[localeIndex])) {
            text.setTextLocale(Locale.forLanguageTag(font.getLanguage()[localeIndex]));
        }
    }

    @Override
    public void onRecycle() {
        if (mTask != null && !mTask.isCancelled()) {
            mTask.cancel(true);
            mTask = null;
        }
    }
}
