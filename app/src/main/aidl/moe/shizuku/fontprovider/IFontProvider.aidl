package moe.shizuku.fontprovider;

import moe.shizuku.fontprovider.FontRequests;
import moe.shizuku.fontprovider.font.BundledFontFamily;
import moe.shizuku.fontprovider.font.FontFamily;

interface IFontProvider {
    ParcelFileDescriptor getFontFileDescriptor(String filename);
    int getFontFileSize(String filename);
    FontFamily[] getFontFamily(String name, in int[] weight);
    String getFontFilePath(String filename);
    BundledFontFamily getBundledFontFamily(in FontRequests requests);
}
