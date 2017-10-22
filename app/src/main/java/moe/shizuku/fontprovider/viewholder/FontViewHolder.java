package moe.shizuku.fontprovider.viewholder;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import moe.shizuku.fontprovider.FontPreviewActivity;
import moe.shizuku.fontprovider.R;
import moe.shizuku.fontprovider.font.FontInfo;
import moe.shizuku.fontprovider.font.FontManager;
import moe.shizuku.support.recyclerview.BaseViewHolder;

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

    private class DownloadBroadcastReceiver extends BroadcastReceiver {

        private List<Long> ids;

        public DownloadBroadcastReceiver(List<Long> ids) {
            this.ids = ids;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id != -1) {
                DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(id));

                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) != DownloadManager.STATUS_SUCCESSFUL) {
                        String title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
                        Toast.makeText(context, context.getString(R.string.toast_download_failed, title), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                ids.remove(id);

                if (ids.isEmpty()) {
                    getAdapter().notifyItemChanged(getAdapterPosition(), new Object());
                    context.unregisterReceiver(this);

                    mBroadcastReceiver = null;
                }
            }
        }
    }

    private DownloadBroadcastReceiver mBroadcastReceiver;

    public FontViewHolder(View itemView) {
        super(itemView);

        title = itemView.findViewById(android.R.id.title);
        summary = itemView.findViewById(android.R.id.summary);
        button = itemView.findViewById(android.R.id.button1);
        size = itemView.findViewById(android.R.id.text1);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDownloadClicked(v);
            }
        });

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!FontManager.getFiles(getData(), v.getContext(), true).isEmpty()) {
                    Toast.makeText(v.getContext(), R.string.font_cannot_preview, Toast.LENGTH_SHORT).show();
                } else {
                    v.getContext().startActivity(FontPreviewActivity.intent(v.getContext(), getData()));
                }
            }
        });

        setIsRecyclable(false);
    }

    private void onDownloadClicked(final View v) {
        ConnectivityManager connectivityManager = (ConnectivityManager) v.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            new AlertDialog.Builder(v.getContext())
                    .setTitle(R.string.dialog_cannot_download)
                    .setMessage(R.string.dialog_no_network)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }

        if (connectivityManager.isActiveNetworkMetered()) {
            new AlertDialog.Builder(v.getContext())
                    .setTitle(R.string.dialog_using_metered_title)
                    .setMessage(R.string.dialog_using_metered_message)
                    .setPositiveButton(R.string.dialog_button_proceed, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startDownload(v, false);
                        }
                    })
                    .setNeutralButton(R.string.dialog_button_download_now, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startDownload(v, true);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();

            return;
        }

        startDownload(v, false);
    }

    private void startDownload(View v, boolean allowDownloadOverMetered) {
        v.setEnabled(false);

        List<String> files = FontManager.getFiles(getData(), v.getContext(), true);
        List<Long> ids = FontManager.download(getData(), files, v.getContext(), allowDownloadOverMetered);

        mBroadcastReceiver = new DownloadBroadcastReceiver(ids);

        FontViewHolder.this.itemView.getContext().registerReceiver(
                mBroadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
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

    @Override
    public void onBind(@NonNull List<Object> payloads) {
        if (FontManager.getFiles(getData(), itemView.getContext(), true).isEmpty()) {
            button.setVisibility(View.GONE);
            size.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRecycle() {
        if (mBroadcastReceiver != null) {
            itemView.getContext().unregisterReceiver(mBroadcastReceiver);
        }
    }
}
