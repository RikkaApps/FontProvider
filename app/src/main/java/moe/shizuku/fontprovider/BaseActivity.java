package moe.shizuku.fontprovider;

import android.app.Activity;
import android.view.MenuItem;

/**
 * Created by rikka on 2017/10/2.
 */

public abstract class BaseActivity extends Activity {

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
