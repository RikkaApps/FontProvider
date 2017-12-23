package moe.shizuku.fontprovider.compat;

import java.nio.ByteBuffer;


public interface FontFamilyImpl {
    Object create(String lang, int variant);
    boolean addFont(Object fontFamily, ByteBuffer font, int ttcIndex, int weight, int italic);
    boolean addFont(Object fontFamily, String path, int weight, int italic);
    boolean freeze(Object fontFamily);
}
