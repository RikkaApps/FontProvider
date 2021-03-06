package moe.haruue.test.fontprovider

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main_content.*
import moe.shizuku.fontprovider.FontProviderClient
import moe.shizuku.fontprovider.FontProviderClient.FontProviderAvailability
import moe.shizuku.fontprovider.compat.TypefaceCompat

class MainActivity : Activity() {

    var init: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        // replace at Activity, only need call once
        if (!init && !App.createInApplication) {
            if (FontProviderClient.checkAvailability(this) == FontProviderAvailability.OK) {
                App.replace(FontProviderClient.create(this)!!)

                init = true
            }
        }

        // only need request permission on 23
        if (Build.VERSION.SDK_INT == 23
                && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        downloadButton.setOnClickListener {
            val intent = with(Intent(Intent.ACTION_VIEW)) {
                data = Uri.parse("https://github.com/RikkaApps/FontProvider/releases/latest")
                this
            }
            startActivity(intent)
        }
        dialogButton.setOnClickListener {
            val dialog = with(AlertDialog.Builder(this@MainActivity)) {
                setTitle(getString(R.string.it_s_rikka))
                setMessage(R.string.rikka)
                setPositiveButton(R.string.ok) { _, _ -> }
                setNegativeButton(R.string.cancel) { _, _ -> }
                create()
            }
            dialog.show()
        }
        recreateButton.setOnClickListener { recreate() }
        p13.text = "盯~ \uD83D\uDE36\uD83D\uDE36\uD83D\uDE36\uD83D\uDE36"
        p14.text = "盯~ \uD83D\uDE36\uD83D\uDE36\uD83D\uDE36\uD83D\uDE36"

        /**
         * To use your custom font (especially weight is not 400) from assets and get correct weigh
         * of other language, you should use TypefaceCompat.createWeightAlias to create Typeface
         * with weight first.
         * Or fonts for will always 400 weight which is very disappointing. (on pre-Oreo)
         */
        h2.typeface = TypefaceCompat.createWeightAlias(Typeface.createFromAsset(assets, "OpenSans-Light.ttf"), 100)
        h2.text = getString(R.string.h2) + " \uD83D\uDE36\uD83D\uDE36\uD83D\uDE36\uD83D\uDE36"
    }
}
