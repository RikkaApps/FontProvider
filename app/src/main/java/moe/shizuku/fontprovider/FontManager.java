package moe.shizuku.fontprovider;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.RawRes;

import com.google.gson.Gson;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moe.shizuku.fontprovider.utils.ContextUtils;

/**
 * Created by rikka on 2017/9/29.
 */

public class FontManager {

    //private static final Map<String, String> sNameToFilename;

    private static final @RawRes int[] FONTS_RES = {
            R.raw.noto_sans_cjk,
            R.raw.noto_serif,
            R.raw.noto_serif_cjk,
    };

    private static final Map<String, FontInfo> FONTS_MAP = new HashMap<>();
    private static final List<FontInfo> FONTS = new ArrayList<>();

    public static void init(Context context) {
        if (FONTS_MAP.size() > 0) {
            return;
        }

        for (int res : FONTS_RES) {
            FontInfo font = new Gson().fromJson(new InputStreamReader(context.getResources().openRawResource(res)), FontInfo.class);
            FONTS_MAP.put(font.getName(), font);
            FONTS.add(font);
        }
    }

    public static List<FontInfo> getFonts() {
        return FONTS;
    }

    public static FontInfo getFont(String name) {
        return FONTS_MAP.get(name);
    }

    public static List<String> getFiles(FontInfo font, Context context, boolean excludeExisted) {
        List<String> files = new ArrayList<>();
        for (FontInfo.Style style : font.getStyle()) {
            if (style.getTtc() != null) {
                files.add(style.getTtc());
            } else {
                files.addAll(Arrays.asList(style.getTtf()));
            }
        }

        if (excludeExisted) {
            for (String f : new ArrayList<>(files)) {
                if (ContextUtils.getFile(context, f).exists()) {
                    files.remove(f);
                }
            }
        }
        return files;
    }

    public static List<Long> download(FontInfo font, List<String> files, Context context) {
        List<Long> ids = new ArrayList<>();

        DownloadManager downloadManager = context.getSystemService(DownloadManager.class);

        for (String f : files) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(font.getUrlPrefix() + f))
                    .setDestinationInExternalFilesDir(context, null, f)
                    .setVisibleInDownloadsUi(false)
                    .setAllowedOverMetered(false)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            ids.add(downloadManager.enqueue(request));
        }

        return ids;
    }

}
