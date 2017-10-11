package moe.shizuku.fontprovider;

import android.content.ServiceConnection;
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

            FontProviderClient.create(this, new FontProviderClient.Callback() {
                @Override
                public boolean onServiceConnected(FontProviderClient client, ServiceConnection serviceConnection) {
                    client.replace("serif", "Noto Sans CJK");
                    client.replace("serif-medium", "Noto Sans CJK");

                    Log.d("Font", "replace costs " + (System.currentTimeMillis() - time) + "ms");
                    return true;
                }
            });

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
