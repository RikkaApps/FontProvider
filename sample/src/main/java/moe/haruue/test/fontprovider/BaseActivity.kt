package moe.haruue.test.fontprovider

import android.app.Activity
import android.os.Bundle
import moe.shizuku.fontprovider.FontProviderClient

abstract class BaseActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!isFontInitialized) {
            FontProviderClient.create(this)?.let { client ->
                client.isNextRequestReplaceFallbackFonts = true
                client.replace("Noto Sans CJK", "sans-serif", "sans-serif-medium")
                client.replace("Noto Serif CJK", "serif", "serif-medium")
                isFontInitialized = true
            }
        }
        super.onCreate(savedInstanceState)
    }

    companion object {

        private var isFontInitialized = false

    }

}