package com.silicornio.googlyeyes.dbandexample.model;

/**
 * Created by silicorniO
 */
public class GEMessageText {

    private int messageTextId;

    private String text;

    public GEMessageText(String text){
        this.text = text;
    }

    @Override
    public String toString() {
        return "GEMessageText{" +
                "messageTextId=" + messageTextId +
                ", text='" + text + '\'' +
                '}';
    }
}
