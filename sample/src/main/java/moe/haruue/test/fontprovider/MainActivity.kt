package moe.haruue.test.fontprovider

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main_content.*
import moe.shizuku.fontprovider.FontProviderClient
import moe.shizuku.fontprovider.FontProviderClient.FontProviderAvailability

class MainActivity : Activity() {

    var init: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        // replace at Activity, only need call once
        if (!init) {
            if (FontProviderClient.checkAvailability(this) == FontProviderAvailability.OK) {
                // To replace correctly after callback called,
                // we must add dummy typefaces not exists in fonts.xml first
                FontProviderClient.create(this, FontProviderClient.Callback { client, _ ->
                    App.replace(client)
                    true
                }, "serif-thin", "serif-light", "serif-medium", "serif-black")

                init = true
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        downloadButton.setOnClickListener {
            val intent = with(Intent(Intent.ACTION_VIEW)) {
                data = Uri.parse("https://github.com/RikkaApps/FontProvider/releases/latest");
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
        p13.text = "盯~ \uD83D\uDE36\uD83D\uDE36\uD83D\uDE36\uD83D\uDE36";
        p14.text = "盯~ \uD83D\uDE36\uD83D\uDE36\uD83D\uDE36\uD83D\uDE36";
    }
}
