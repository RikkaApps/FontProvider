package moe.shizuku.notocjk.provider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Locale;

import moe.shizuku.notocjk.provider.api.TypefaceReplacer;

public class MainActivity extends Activity {

    @SuppressLint({"PrivateApi", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TypefaceReplacer.init(this,
                new String[]{"sans-serif-light", "sans-serif-medium", "serif", "serif-medium", "serif-light"});

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView title = findViewById(R.id.text_title);
        final TextView textSans300 = findViewById(R.id.text_sans_serif_300);
        final TextView textSans400 = findViewById(R.id.text_sans_serif_400);
        final TextView textSans500 = findViewById(R.id.text_sans_serif_500);
        final TextView textSerif300 = findViewById(R.id.text_serif_300);
        final TextView textSerif400 = findViewById(R.id.text_serif_400);
        final TextView textSerif500 = findViewById(R.id.text_serif_500);

        title.setText(Locale.getDefault().toLanguageTag());

        String testText = "This is a sample text. 骨曜将葛，地玄系、片海示。";
        textSans300.setText("300 " + testText);
        textSans400.setText("400 " + testText);
        textSans500.setText("500 " + testText);
        textSerif300.setText("300 " + testText);
        textSerif400.setText("500 " + testText);
        textSerif500.setText("500 " + testText);

        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                textSans300.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                textSans400.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                textSans500.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

                textSerif300.setTypeface(Typeface.create("serif-light", Typeface.NORMAL));
                textSerif400.setTypeface(Typeface.create("serif", Typeface.NORMAL));
                textSerif500.setTypeface(Typeface.create("serif-medium", Typeface.NORMAL));
            }
        }, 500);


    }
}
