package moe.shizuku.fontprovider;

import moe.shizuku.fontprovider.font.FontFamily;

interface IFontProvider {
    ParcelFileDescriptor getFontFileDescriptor(String filename);
    int getFontFileSize(String filename);
    FontFamily[] getFontFamily(String name, in int[] weight);
    String getFontFilePath(String filename);
}
