package com.gse23.lhusemann;

public class Images {
    protected String id;
    protected String contentType;
    protected String link;

    public Images() {
    }
    public Images(String id, String contentType, String link) {
        this.id = id;
        this.contentType = contentType;
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public String getContentType() {
        return contentType;
    }
    public String getLink() {
        return link;
    }
}
