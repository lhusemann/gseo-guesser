package com.gse23.lhusemann;

public class serverAlbum {
    protected String success;
    protected String message;
    protected String id;
    protected String name;
    protected Images[] images;

    public serverAlbum() {
    }
    public serverAlbum(String success, String message, String id, String name, Images[] images) {
        this.success = success;
        this.message = message;
        this.id = id;
        this.name = name;
        this.images = images;
    }

    public String getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public Images[] getImages() {
        return images;
    }
}
