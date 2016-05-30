package com.silicornio.googlyeyes.dbandexample.model;

/**
 * Created by SilicorniO
 */
public class GEMessageEncrypt {

    private String title;
    public GEMessageEncrypt(String title){
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "GEMessageEncrypt{" +
                "title='" + title + '\'' +
                '}';
    }
}
