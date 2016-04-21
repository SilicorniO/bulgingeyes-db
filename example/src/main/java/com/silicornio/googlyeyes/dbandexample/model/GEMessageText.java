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

    public int getMessageTextId() {
        return messageTextId;
    }

    public void setMessageTextId(int messageTextId) {
        this.messageTextId = messageTextId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GEMessageText that = (GEMessageText) o;

        return text.equals(that.text);

    }

    @Override
    public int hashCode() {
        int result = messageTextId;
        result = 31 * result + text.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "GEMessageText{" +
                "messageTextId=" + messageTextId +
                ", text='" + text + '\'' +
                '}';
    }
}
