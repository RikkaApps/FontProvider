package moe.shizuku.fontprovider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Locale;

import moe.shizuku.fontprovider.adapter.FontPreviewAdapter;
import moe.shizuku.fontprovider.font.FontInfo;
import moe.shizuku.support.recyclerview.RecyclerViewHelper;

/**
 * Created by rikka on 2017/10/3.
 */

public class FontPreviewActivity extends BaseActivity {

    private static final int MENU_ITEM_ID_START = 0x10000;

    private FontInfo mFontInfo;
    private int mLocaleIndex;
    private FontPreviewAdapter mAdapter;

    public static Intent intent(Context context, FontInfo font) {
        return new Intent(context, FontPreviewActivity.class)
                .putExtra(Intents.EXTRA_DATA, font);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_list);

        mFontInfo = getIntent().getParcelableExtra(Intents.EXTRA_DATA);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(mFontInfo.getName());
        }

        RecyclerView recyclerView = findViewById(android.R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mAdapter = new FontPreviewAdapter(mFontInfo);
        recyclerView.setAdapter(mAdapter);

        RecyclerViewHelper.fixOverScroll(recyclerView);

        if (savedInstanceState != null) {
            onLocaleChanged(savedInstanceState.getInt(Intents.EXTRA_LOCALE_INDEX, 0));
        } else {
            onLocaleChanged(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mFontInfo.getLanguage()[0] != null) {
            getMenuInflater().inflate(R.menu.preview, menu);

            MenuItem item = menu.findItem(R.id.action_language);
            String[] language = mFontInfo.getLanguage();
            for (int i = 0; i < language.length; i++) {
                item.getSubMenu().add(0, MENU_ITEM_ID_START + i, i, Locale.forLanguageTag(language[i]).getDisplayName());
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String[] language = mFontInfo.getLanguage();
        if (language[0] != null
                && id >= MENU_ITEM_ID_START && id < MENU_ITEM_ID_START + language.length) {
            onLocaleChanged(id - MENU_ITEM_ID_START);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Intents.EXTRA_LOCALE_INDEX, mLocaleIndex);
    }

    private void onLocaleChanged(int localeIndex) {
        mLocaleIndex = localeIndex;

        if (mFontInfo.getLanguage()[0] != null) {
            Locale locale = Locale.forLanguageTag(mFontInfo.getLanguage()[mLocaleIndex]);

            if (getActionBar() != null) {
                getActionBar().setSubtitle(locale.getDisplayName());
            }
        }

        mAdapter.setLocaleIndex(mLocaleIndex);
        mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount(), new Object());
    }
}
