package moe.haruue.test.fontprovider

import android.app.Application
import android.util.Log
import moe.shizuku.fontprovider.FontProviderClient
import moe.shizuku.fontprovider.FontRequest
import moe.shizuku.fontprovider.FontRequests

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // async, fonts in first activity will not be replaced
        /*FontProviderClient.create(this) {
            client, _ ->
            replace(client)
            return@create true
        }*/

        // sync, very slow!!!
        //replace(FontProviderClient.createSync(this))
    }

    companion object {
        fun replace(client: FontProviderClient) {
            FontRequests.setDefaultSansSerifFonts(FontRequest.DEFAULT, FontRequest.NOTO_COLOR_EMOJI)
            FontRequests.setDefaultSerifFonts(FontRequest.NOTO_SERIF, FontRequest.NOTO_COLOR_EMOJI_NOUGAT)

            val time = System.currentTimeMillis()
            client.replace("sans-serif", "Noto Sans CJK")
            client.replace("sans-serif-thin", "Noto Sans CJK")
            client.replace("sans-serif-light", "Noto Sans CJK")
            client.replace("sans-serif-medium", "Noto Sans CJK")
            client.replace("sans-serif-black", "Noto Sans CJK")
            client.replace("serif", "Noto Serif CJK")
            client.replace("serif-thin", "Noto Serif CJK")
            client.replace("serif-light", "Noto Serif CJK")
            client.replace("serif-medium", "Noto Serif CJK")
            client.replace("serif-black", "Noto Serif CJK")
            Log.d("FontProvider", "replace costs " + (System.currentTimeMillis() - time) + "ms")
        }
    }
}