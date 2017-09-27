package moe.shizuku.notocjk.provider;

interface IFontProvider {
    ParcelFileDescriptor getFontFileDescriptor(String filename);
    int getFontFileSize(String filename);
}
