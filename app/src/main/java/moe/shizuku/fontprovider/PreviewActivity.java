package moe.shizuku.fontprovider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class PreviewActivity extends Activity {

    private static boolean init;

    @SuppressLint({"SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!init) {
            FontProviderClient.create(this, new FontProviderClient.Callback() {
                @Override
                public void onServiceConnected(FontProviderClient client) {
                    client.replace("sans-serif-thin",   "Noto Sans CJK");
                    client.replace("sans-serif-light",  "Noto Sans CJK");
                    client.replace("sans-serif",        "Noto Sans CJK");
                    client.replace("sans-serif-medium", "Noto Sans CJK");
                    client.replace("sans-serif-black",  "Noto Sans CJK");

                    client.replace("serif-thin",        "Noto Serif CJK");
                    client.replace("serif-light",       "Noto Serif CJK");
                    client.replace("serif",             "Noto Serif CJK");
                    client.replace("serif-medium",      "Noto Serif CJK");
                    client.replace("serif-black",       "Noto Serif CJK");
                }
            });

            init = true;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        findViewById(android.R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FontManager.downloadAll(view.getContext());
            }
        });

        if (FontManager.getMissingFiles(this).isEmpty()) {
            findViewById(android.R.id.button1).setVisibility(View.GONE);
        } else {
            Toast.makeText(this, "You should download missing fonts", Toast.LENGTH_SHORT).show();
        }

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

        String testText = getString(R.string.preview_text);
        textSans100.setText(testText);
        textSans300.setText(testText);
        textSans400.setText(testText);
        textSans500.setText(testText);
        textSans700.setText(testText);
        textSans900.setText(testText);
        textSerif100.setText(testText);
        textSerif300.setText(testText);
        textSerif400.setText(testText);
        textSerif500.setText(testText);
        textSerif700.setText(testText);
        textSerif900.setText(testText);

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

        FontProviderService.closeAll();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_hide:
                getPackageManager().setComponentEnabledSetting(new ComponentName(this, PreviewActivity.class),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                return true;
            case R.id.action_download:
                FontManager.downloadAll(this);
            case R.id.action_free:
                FontProviderService.closeAll();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
