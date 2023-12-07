package com.gse23.lhusemann;

import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.io.InputStream;

public final class ReadExif {
    private ReadExif() {
    }
    public static String getData(String exifTag, InputStream in) throws IOException {
        ExifInterface exifInterface = new ExifInterface(in);
        return exifInterface.getAttribute(exifTag);
    }
    public static boolean corrupted(String exifData, String exifTag) {
        return exifData == null && !(exifTag.equals(ExifInterface.TAG_IMAGE_DESCRIPTION));
    }
}
