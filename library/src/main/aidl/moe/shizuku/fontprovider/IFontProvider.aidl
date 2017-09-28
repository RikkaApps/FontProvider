package moe.shizuku.fontprovider;

interface IFontProvider {
    ParcelFileDescriptor getFontFileDescriptor(String filename);
    int getFontFileSize(String filename);
}
