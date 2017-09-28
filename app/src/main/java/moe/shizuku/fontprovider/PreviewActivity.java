package moe.shizuku.fontprovider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Locale;

import moe.shizuku.fontprovider.api.TypefaceReplacer;

public class PreviewActivity extends Activity {

    @SuppressLint({"SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // init only need be called once
        TypefaceReplacer.init(this,
                TypefaceReplacer.NOTO_SANS_CJK_LIGHT,
                TypefaceReplacer.NOTO_SANS_CJK_MEDIUM,
                TypefaceReplacer.NOTO_SERIF_CJK_LIGHT,
                TypefaceReplacer.NOTO_SERIF_CJK_REGULAR,
                TypefaceReplacer.NOTO_SERIF_CJK_MEDIUM);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        final TextView title = findViewById(R.id.text_title);
        final TextView textSans100 = findViewById(R.id.text_sans_serif_100);
        final TextView textSans300 = findViewById(R.id.text_sans_serif_300);
        final TextView textSans400 = findViewById(R.id.text_sans_serif_400);
        final TextView textSans500 = findViewById(R.id.text_sans_serif_500);
        final TextView textSans700 = findViewById(R.id.text_sans_serif_700);
        final TextView textSans900 = findViewById(R.id.text_sans_serif_900);
        final TextView textSerif100 = findViewById(R.id.text_serif_100);
        final TextView textSerif300 = findViewById(R.id.text_serif_300);
        final TextView textSerif400 = findViewById(R.id.text_serif_400);
        final TextView textSerif500 = findViewById(R.id.text_serif_500);
        final TextView textSerif700 = findViewById(R.id.text_serif_700);
        final TextView textSerif900 = findViewById(R.id.text_serif_900);

        title.setText(Locale.getDefault().toLanguageTag());

        String testText = "This is a sample text. 骨曜将葛，地玄系、片海示。";
        textSans100.setText("100 " + testText);
        textSans300.setText("300 " + testText);
        textSans400.setText("400 " + testText);
        textSans500.setText("500 " + testText);
        textSans700.setText("700 " + testText);
        textSans900.setText("900 " + testText);
        textSerif100.setText("100 " + testText);
        textSerif300.setText("300 " + testText);
        textSerif400.setText("400 " + testText);
        textSerif500.setText("500 " + testText);
        textSerif700.setText("700 " + testText);
        textSerif900.setText("900 " + testText);

        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                textSans100.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
                textSans300.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                textSans400.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                textSans500.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                textSans700.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                textSans900.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));

                textSerif100.setTypeface(Typeface.create("serif-thin", Typeface.NORMAL));
                textSerif300.setTypeface(Typeface.create("serif-light", Typeface.NORMAL));
                textSerif400.setTypeface(Typeface.create("serif", Typeface.NORMAL));
                textSerif500.setTypeface(Typeface.create("serif-medium", Typeface.NORMAL));
                textSerif700.setTypeface(Typeface.create("serif", Typeface.BOLD));
                textSerif900.setTypeface(Typeface.create("serif-black", Typeface.NORMAL));
            }
        }, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // not init in Application so need unbind manually
        TypefaceReplacer.unbind();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_hide:
                getPackageManager().setComponentEnabledSetting(new ComponentName(this, PreviewActivity.class),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
