package com.gse23.lhusemann;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.util.Log;

import java.io.IOException;

import java.io.InputStream;

/**
 * Das ist die MainActivity, die zuerst ausgeführt wird, wenn die App gestartet wird.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            if (getAssets().list("albums").length < 1)
            {
                Log.i("Keine Dateien in albums vorhanden", "Keine Dateien in albums vorhanden");
            }
            else
            {
                this.showAllImagesInAlbums();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String[] getFolderInhalt (String folder) throws IOException
    {
        return getAssets().list(folder);
    }
    private void showAllImagesInFolder(String[] files,String folder)
    {
        for (String file : files) {
            if (file.contains(".jpeg") || file.contains(".jpg") || file.contains(".png"))
                Log.i("Foto-Ausgabe","Im Ordner " + folder + " liegt das Foto: "+ file);
        }
    }
    private void showAllImagesInAlbums() throws IOException
    {
        String[] alben = this.getFolderInhalt("albums");
        for (String folder : alben) {
            this.showAllImagesInFolder(getFolderInhalt("albums/" + folder), folder);
        }
    }
}
