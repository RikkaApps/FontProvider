package moe.shizuku.notocjk.provider.api;

import moe.shizuku.notocjk.provider.IFontProvider;

/**
 * Created by rikka on 2017/9/27.
 */

public abstract class TypefaceReplacerImpl {

    public abstract boolean replace(IFontProvider fontProvider, String family);
}
