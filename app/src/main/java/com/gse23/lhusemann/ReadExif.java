package com.gse23.lhusemann;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import androidx.exifinterface.media.ExifInterface;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ReadExif {
    private static final int TAGLAENGE = 3;
    private static final String BINDESTRICH = "-";
    private final ArrayList<Bild> album = new ArrayList<>();
    public ReadExif() {
    }
    //erstellt das Album aus allen Bilder des Ordners
    public void getImages(Activity activity, boolean downloaded, String name) {
        String[] inhaltInOrdner;
        if (downloaded) {
            inhaltInOrdner = getBilderNamenDownloaded(activity.getFilesDir() + "/Album/" + name);
        } else {
            inhaltInOrdner = getBilderNamenAssets(name, activity);
        }
        addAlbum(inhaltInOrdner, name, activity, downloaded);
    }
    //gibt ein Array von Ordnern/Dateien an bestimmer Stelle aus
    public static String[] getBilderNamenAssets(String folder, Activity activity) {
        try {
            return activity.getAssets().list("albums/" + folder);
        } catch (IOException e) {
            return new String[0];
        }
    }
    public static String[] getBilderNamenDownloaded(String folderLocation) {
        File albumOrdner = new File(folderLocation);
        File[] albumArray = albumOrdner.listFiles();
        if (albumArray != null) {
            String[] names = new String[albumArray.length];
            for (int i = 0; i < albumArray.length; i++) {
                names[i] = albumArray[i].getName();
            }
            return names;
        } else {
            return new String[0];
        }
    }
    public void addBild(String file, String album, boolean downloaded, Activity activity) {
        if (downloaded) {
            addBildDownloaded(album, file, activity);
        } else {
            addBildAssets(album, file, activity);
        }
    }
    //gibt Exifdaten zurück mit Hilfe der Klasse ReadExif
    public void addBildDownloaded(String album, String file, Activity activity) {
        try {
            ExifInterface exifInterface = new ExifInterface(activity.getFilesDir() + "/Album/" + album + "/" + file);
            finalAddBild(exifInterface, file, activity);
        } catch (IOException e) {
            Toast.makeText(activity.getApplicationContext(), "Fehler beim Lesen eines Bildes!", Toast.LENGTH_SHORT).show();
            activity.finish();
        }
    }
    public void addBildAssets(String album, String file, Activity activity) {
        try (InputStream in = activity.getAssets().open("albums/" + album + "/" + file)) {
            ExifInterface exifInterface = new ExifInterface(in);
            finalAddBild(exifInterface, file, activity);
        } catch (IOException e) {
            Toast.makeText(activity, "Fehler beim Lesen eines Bildes!", Toast.LENGTH_SHORT).show();
            activity.finish();
        }
    }
    public void addAlbum(String[] names, String album, Activity activity, boolean downloaded) {
        boolean noImageException = true;
        for (String name : names) {
            if (Bild.isImage(name)) {
                noImageException = false;
                addBild(name, album, downloaded, activity);
            }
        }
        if (noImageException) {
            try {
                throw new NoImagesInAlbumException();
            } catch (NoImagesInAlbumException e) {
                Log.i("NoImagesInAlbumException", "Das Album hat keine Bilddateien enthalten.");
                Toast.makeText(activity, "Keine Bilder im ausgewaehltem Album vorhanden.", Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        }
    }
    public void finalAddBild(ExifInterface exifInterface, String file, Activity activity) {
        String description = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
        float[] koords = new float[2];
        try {
            double latitude;
            double longitude;
            if (exifInterface.getLatLong(koords)) {
                latitude = (double) koords[0];
                longitude = (double) koords[1];
            } else {
                throw new CorruptedExifDataException();
            }
            Log.i("Name", file);
            if (description == null) {
                Log.i("Keine Beschreibung vorhanden", "-");
            } else {
                Log.i("Beschreibung",description);
            }
            Log.i("Breitengrad",Double.toString(latitude));
            Log.i("Laengengrad",Double.toString(longitude));
            album.add(new Bild(file, description, longitude, latitude));
        } catch (CorruptedExifDataException e) {
            Log.i("CorruptedExifDataException", "Koordinaten konnten in der Datei " + file + " nicht gefunden werden.");
            Toast.makeText(activity, "Koordinaten in " + file + " nicht gefunden werden.", Toast.LENGTH_SHORT).show();
            activity.finish();
        }
    }
    public ArrayList<Bild> getBilder() {
        return album;
    }

    //Da in MS0 gefordert war, die Exifdaten im Assetsordner auszugeben ohne exceptions zuwerfen, mache ich das hier
    public static void logExifAssets(String folderpath, String exifTag, Activity activity) {
        String filepath = "albums/" + folderpath;
        try (InputStream in = activity.getAssets().open(filepath)) {
            ExifInterface exifInterface = new ExifInterface(in);
            String exifData = exifInterface.getAttribute(exifTag);
            if (exifData == null) {
                Log.i(exifTag.substring(TAGLAENGE) + " nicht vorhanden", BINDESTRICH);
            } else {
                Log.i(exifTag.substring(TAGLAENGE) + " vorhanden", exifData);
            }
        } catch (IOException ignored) {
            Log.i("Exifdaten-Fehler", "pfad: " + filepath);
            Toast.makeText(activity, "Fehler beim Laden von Exifdaten", Toast.LENGTH_SHORT).show();
        }
    }
    //gibt anzahl der Bilder in einem Ordner wieder
    public static int getImagesCount(String folder, boolean downloaded, Activity activity) {
        int counter = 0;
        String[] images;
        if (downloaded) {
            images = getBilderNamenDownloaded(activity.getFilesDir() + "/Album/" + folder);
        } else {
            images = getBilderNamenAssets(folder, activity);
        }
        for (String file : images) {
            if (Bild.isImage(file)) {
                counter++;
            }
        }
        return counter;
    }
}
