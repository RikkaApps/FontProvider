package moe.shizuku.fontprovider;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import moe.shizuku.fontprovider.adapter.FontManageAdapter;
import moe.shizuku.fontprovider.font.FontManager;
import moe.shizuku.utils.recyclerview.helper.RecyclerViewHelper;

/**
 * Created by rikka on 2017/10/2.
 */

public class FontManageActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_list);

        RecyclerView recyclerView = findViewById(android.R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        FontManageAdapter adapter = new FontManageAdapter(FontManager.getFonts());
        recyclerView.setAdapter(adapter);

        RecyclerViewHelper.fixOverScroll(recyclerView);
    }
}
