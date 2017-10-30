package moe.shizuku.fontprovider;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;

/**
 * Created by rikka on 2017/10/2.
 */

public abstract class BaseActivity extends FragmentActivity {

    private static boolean sFontInitialized = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (!sFontInitialized) {
            final long time = System.currentTimeMillis();

            FontProviderClient client = FontProviderClient.create(this);
            client.replace(FontRequests.DEFAULT_SANS_SERIF_FONTS, "Noto Sans CJK",
                    "sans-serif", "sans-serif-medium");

                    Log.d("Font", "replace costs " + (System.currentTimeMillis() - time) + "ms");

            sFontInitialized = true;
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
