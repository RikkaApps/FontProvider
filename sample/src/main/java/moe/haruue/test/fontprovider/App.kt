package moe.haruue.test.fontprovider

import android.app.Application
import android.util.Log
import moe.shizuku.fontprovider.FontProviderClient

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        FontProviderClient.create(this) {
            client ->
            val time = System.currentTimeMillis()
            client.replace("sans-serif", "Noto Sans CJK")
            client.replace("sans-serif-thin", "Noto Sans CJK")
            /*client.replace("sans-serif-light", "Noto Sans CJK")
            client.replace("sans-serif-regular", "Noto Sans CJK")
            client.replace("sans-serif-medium", "Noto Sans CJK")
            client.replace("sans-serif-bold", "Noto Sans CJK")
            client.replace("sans-serif-black", "Noto Sans CJK")
            client.replace("serif", "Noto Serif CJK")
            client.replace("serif-thin", "Noto Serif CJK")
            client.replace("serif-light", "Noto Serif CJK")
            client.replace("serif-regular", "Noto Serif CJK")
            client.replace("serif-medium", "Noto Serif CJK")
            client.replace("serif-bold", "Noto Serif CJK")
            client.replace("serif-black", "Noto Serif CJK")*/
            Log.d("FontProvider", "replace costs " + (System.currentTimeMillis() - time) + "ms")
            return@create true
        }
    }

}