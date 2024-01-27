package com.gse23.lhusemann;

import android.content.Context;
import android.util.Log;
import androidx.exifinterface.media.ExifInterface;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ReadExif {
    private static final int TAGLAENGE = 3;
    private static final String BINDESTRICH = "-";
    private final ArrayList<Bild> album = new ArrayList<>();
    private boolean corruptedExifException = false;
    private boolean noImageException = true;
    private boolean ioException = false;
    private boolean downloaded;
    private Context activity;
    private String albumName;
    public ReadExif(Context activity, boolean downloaded, String albumName) {
        this.activity = activity;
        this.downloaded = downloaded;
        this.albumName = albumName;
    }
    public void startGetInhaltInOrdner() throws CorruptedExifDataException, NoImagesInAlbumException, IOException {
        String[] inhaltInOrdner;
        if (downloaded) {
            inhaltInOrdner = getNamenDownloaded(activity.getFilesDir() + "/Album/" + albumName);
        } else {
            inhaltInOrdner = getNamenAssets(albumName, activity);
        }
        addAlbum(inhaltInOrdner);
        if (corruptedExifException) {
            throw new CorruptedExifDataException();
        }
        if (noImageException) {
            throw new NoImagesInAlbumException();
        }
        if (ioException) {
            throw new IOException("Fehler beim Lesen eines Bildes!");
        }
    }

    public void addAlbum(String[] names) {
        for (String name : names) {
            if (Bild.isImage(name)) {
                noImageException = false;
                addBild(name);
            }
        }
    }
    public void addBild(String file) {
        if (downloaded) {
            addBildDownloaded(file);
        } else {
            addBildAssets(file);
        }
    }
    //gibt Exifdaten zurück mit Hilfe der Klasse ReadExif
    public void addBildDownloaded(String file) {
        try {
            ExifInterface exifInterface = new ExifInterface(activity.getFilesDir() + "/Album/" + albumName + "/" + file);
            finalAddBild(exifInterface, file);
        } catch (IOException e) {
            ioException = true;
        }
    }
    public void addBildAssets(String file) {
        try (InputStream in = activity.getAssets().open("albums/" + albumName + "/" + file)) {
            ExifInterface exifInterface = new ExifInterface(in);
            finalAddBild(exifInterface, file);
        } catch (IOException e) {
            ioException = true;
        }
    }
    public void finalAddBild(ExifInterface exifInterface, String file) {
        String description = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
        float[] koords = new float[2];
        double latitude;
        double longitude;
        if (exifInterface.getLatLong(koords)) {
            latitude = (double) koords[0];
            longitude = (double) koords[1];
            Log.i("Name", file);
            if (description == null) {
                Log.i("Keine Beschreibung vorhanden", "-");
            } else {
                Log.i("Beschreibung",description);
            }
            Log.i("Breitengrad",Double.toString(latitude));
            Log.i("Laengengrad",Double.toString(longitude));
            album.add(new Bild(file, description, longitude, latitude));
        } else {
            corruptedExifException = true;
        }
    }
    public ArrayList<Bild> getBilder() {
        return album;
    }
    public boolean anyException() {
        return corruptedExifException || ioException || noImageException;
    }
    //Da in MS0 gefordert war, die Exifdaten im Assetsordner auszugeben ohne exceptions zuwerfen, mache ich das hier
    public static void logExifAssets(String folderpath, String exifTag, Context activity) {
        String filepath = "albums/" + folderpath;
        try (InputStream in = activity.getAssets().open(filepath)) {
            ExifInterface exifInterface = new ExifInterface(in);
            String exifData = exifInterface.getAttribute(exifTag);
            if (exifData == null) {
                Log.i(exifTag.substring(TAGLAENGE) + " nicht vorhanden", BINDESTRICH);
            } else {
                Log.i(exifTag.substring(TAGLAENGE) + " vorhanden", exifData);
            }
        } catch (IOException e) {
            Log.i("Exifdaten-Fehler", "pfad: " + filepath);
        }
    }
    //gibt anzahl der Bilder in einem Ordner wieder
    public static int getImagesCount(String folder, boolean downloaded, Context activity) {
        int counter = 0;
        String[] images;
        if (downloaded) {
            images = getNamenDownloaded(activity.getFilesDir() + "/Album/" + folder);
        } else {
            images = getNamenAssets(folder, activity);
        }
        for (String file : images) {
            if (Bild.isImage(file)) {
                counter++;
            }
        }
        return counter;
    }

    //gibt ein Array von Ordnern/Dateien an bestimmer Stelle aus Album wieder
    public static String[] getNamenAssets(String folder, Context activity) {
        try {
            return activity.getAssets().list("albums/" + folder);
        } catch (IOException e) {
            Log.i("Fehler","keine Namen in albums/" + folder + "gefunden");
            return new String[0];
        }
    }
    //gibt ein Array von Ordnern/Dateien an bestimmer Stelle aus dem internen Speicher aus
    public static String[] getNamenDownloaded(String folderLocation) {
        File albumOrdner = new File(folderLocation);
        File[] albumArray = albumOrdner.listFiles();
        if (albumArray != null) {
            String[] names = new String[albumArray.length];
            for (int i = 0; i < albumArray.length; i++) {
                names[i] = albumArray[i].getName();
            }
            return names;
        } else {
            Log.i("Fehler","keine Namen in " + folderLocation + "gefunden");
            return new String[0];
        }
    }
}
