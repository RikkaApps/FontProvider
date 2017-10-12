package moe.shizuku.fontprovider;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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

        int localeIndex = getIntent().getIntExtra(Intents.EXTRA_LOCALE_INDEX, 0);
        Context context = null;
        if (mFontInfo.getLanguage()[0] != null) {
            Locale locale = Locale.forLanguageTag(mFontInfo.getLanguage()[localeIndex]);

            Configuration configuration = new Configuration(getResources().getConfiguration());
            configuration.setLocale(locale);
            context = createConfigurationContext(configuration);
            context.setTheme(mThemeId);

            if (getActionBar() != null) {
                getActionBar().setSubtitle(locale.getDisplayName());
            }
        }

        RecyclerView recyclerView = findViewById(android.R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        FontPreviewAdapter adapter = new FontPreviewAdapter(mFontInfo, mFontInfo.getIndex()[localeIndex], context);
        recyclerView.setAdapter(adapter);

        RecyclerViewHelper.fixOverScroll(recyclerView);
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
            getIntent().putExtra(Intents.EXTRA_LOCALE_INDEX, id - MENU_ITEM_ID_START);
            getWindow().setWindowAnimations(R.style.Animation_FadeInOut);
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private int mThemeId;

    @Override
    public void setTheme(int resid) {
        super.setTheme(resid);
        mThemeId = resid;
    }
}
