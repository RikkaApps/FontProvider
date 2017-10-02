package moe.shizuku.fontprovider.viewholder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import moe.shizuku.fontprovider.FontInfo;
import moe.shizuku.fontprovider.FontManager;
import moe.shizuku.fontprovider.FontPreviewActivity;
import moe.shizuku.fontprovider.R;
import moe.shizuku.utils.recyclerview.BaseViewHolder;

/**
 * Created by rikka on 2017/10/2.
 */

public class FontViewHolder extends BaseViewHolder<FontInfo> {

    public static final Creator<FontInfo> CREATOR = new Creator<FontInfo>() {
        @Override
        public BaseViewHolder<FontInfo> createViewHolder(LayoutInflater inflater, ViewGroup parent) {
            return new FontViewHolder(inflater.inflate(R.layout.item_font, parent, false));
        }
    };

    private TextView title;
    private TextView summary;
    private TextView size;
    private View button;

    public FontViewHolder(View itemView) {
        super(itemView);

        title = itemView.findViewById(android.R.id.title);
        summary = itemView.findViewById(android.R.id.summary);
        button = itemView.findViewById(android.R.id.button1);
        size = itemView.findViewById(android.R.id.text1);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> files = FontManager.getFiles(getData(), v.getContext(), true);
                FontManager.download(getData(), files, v.getContext());
            }
        });

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(FontPreviewActivity.intent(v.getContext(), getData()));
            }
        });
    }

    @Override
    public void onBind() {
        Context context = itemView.getContext();
        FontInfo font = getData();

        if (FontManager.getFiles(getData(), itemView.getContext(), true).isEmpty()) {
            button.setVisibility(View.GONE);
            size.setVisibility(View.GONE);
        }

        title.setText(font.getName());

        // TODO do not use String
        size.setText(font.getSize());

        if (font.getLanguage()[0] != null) {
            summary.setText(context.getString(R.string.font_summary, font.getStyle().length, font.getLanguage().length));
        } else {
            summary.setText(context.getString(R.string.font_summary_no_language, font.getStyle().length));
        }
    }
}
