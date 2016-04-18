package com.silicornio.googlyeyes.dbandexample.model;

/**
 * Created by SilicorniO
 */
public class GEMessage {

    private int messageId;

    private String title;

    private GEMessageText text;

    public GEMessage(String title, GEMessageText text){
        this.title = title;
        this.text = text;
    }

    @Override
    public String toString() {
        return "GEMessage{" +
                "messageId=" + messageId +
                ", title='" + title + '\'' +
                ", text=" + text +
                '}';
    }
}
