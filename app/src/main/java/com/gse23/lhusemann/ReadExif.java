package com.gse23.lhusemann;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import androidx.exifinterface.media.ExifInterface;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public final class ReadExif {
    private static final String INTERNALB = "/Album/";
    private static final String ALBUMASSETS = "albums";
    private static final int TAGLAENGE = 3;
    private static final String BINDESTRICH = "-";
    private ReadExif() {
    }
    public static ArrayList<String> getPath(Context ctx, String folder, boolean downloaded) {
        ArrayList<String> paths = new ArrayList<>();
        if (downloaded) {
            String vorPath = ctx.getFilesDir() + INTERNALB + folder;

            for (String file : getFolderInhalt(vorPath, ctx, true)) {
                if (Bild.isImage(file)) {
                    paths.add(vorPath + "/" + file);
                }
            }
        } else {
            String vorPath = ALBUMASSETS + "/" + folder;
            for (String file : getFolderInhalt(vorPath, ctx, false)) {
                if (Bild.isImage(file)) {
                    paths.add(vorPath + "/" + file);
                }
            }
        }
        return paths;
    }
    //gibt Namen aller Bilder in einem Array wieder
    public static String[] getNames(Context ctx, String folder, boolean downloaded) {
        ArrayList<String> paths = new ArrayList<>();
        if (downloaded) {
            for (String file : getFolderInhalt(ctx.getFilesDir() + INTERNALB + folder, ctx, true)) {
                if (Bild.isImage(file)) {
                    paths.add(file);
                }
            }
        } else {
            for (String file : getFolderInhalt(ALBUMASSETS + "/" + folder, ctx, false)) {
                if (Bild.isImage(file)) {
                    paths.add(file);
                }
            }
        }
        return paths.toArray(new String[0]);
    }
    //gibt Inhalt eines Ordner wieder ohne Fehlermeldung
    public static String[] getFolderInhalt(String folder, Context ctx, boolean downloaded) {
        if (downloaded) {
            File albumOrdner = new File(folder);
            File[] albumArray = albumOrdner.listFiles();
            String[] names = new String[albumArray.length];
            for (int i = 0; i < albumArray.length; i++) {
                names[i] = albumArray[i].getName();
            }
            return names;
        } else {
            try {
                return ctx.getAssets().list(folder);
            } catch (IOException e) {
                return new String[0];
            }
        }
    }
    //gibt Exifdaten zurück mit Hilfe der Klasse ReadExif
    public static String getExifdata(String filepath, String exifTag, Activity activity, boolean downloaded) {
        if (downloaded) {
            try {
                ExifInterface exifInterface = new ExifInterface(filepath);
                String exifData = exifInterface.getAttribute(exifTag);
                if (exifData == null && !(exifTag.equals(ExifInterface.TAG_IMAGE_DESCRIPTION))) {
                    throw new CorruptedExifDataException(exifTag.substring(TAGLAENGE), filepath, activity);
                } else {
                    if (exifData == null) {
                        return "";
                    } else {
                        return vorzeichen(exifInterface, exifTag) + exifData;
                    }
                }
            } catch (CorruptedExifDataException | IOException e) {
                return "";
            }
        } else {
            try (InputStream in = activity.getAssets().open(filepath)) {
                ExifInterface exifInterface = new ExifInterface(in);
                String exifData = exifInterface.getAttribute(exifTag);
                if (exifData == null && !(exifTag.equals(ExifInterface.TAG_IMAGE_DESCRIPTION))) {
                    throw new CorruptedExifDataException(exifTag.substring(TAGLAENGE), filepath, activity);
                } else {
                    return exifData;
                }
            } catch (CorruptedExifDataException | IOException e) {
                return "";
            }
        }
    }
    //prueft ob es sich um ein negatives vorzeichen handelt
    private static String vorzeichen(ExifInterface exifInterface, String exifTag) {
        if (exifTag.equals(ExifInterface.TAG_GPS_LONGITUDE)) {
            if (Objects.equals(exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF), "W")) {
                return BINDESTRICH;
            }
            if (Objects.equals(exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF), "E")) {
                return "";
            }
        } else if (exifTag.equals(ExifInterface.TAG_GPS_LATITUDE)) {
            if (Objects.equals(exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF), "S")) {
                return BINDESTRICH;
            }
            if (Objects.equals(exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF), "N")) {
                return "";
            }
        }
        return "";
    }

    //Da in MS0 gefordert war, die Exifdaten im Assetsordner auszugeben ohne exceptions zuwerfen, mache ich das hier
    public static void logExifAssets(String filepath, String exifTag, Activity activity) {
        try (InputStream in = activity.getAssets().open(filepath)) {
            ExifInterface exifInterface = new ExifInterface(in);
            String exifData = exifInterface.getAttribute(exifTag);
            if (exifData == null) {
                Log.i(exifTag.substring(TAGLAENGE) + " nicht vorhanden", BINDESTRICH);
            } else {
                Log.i(exifTag.substring(TAGLAENGE) + " vorhanden", exifData);
            }
        } catch (IOException ignored) {
        }
    }
}
