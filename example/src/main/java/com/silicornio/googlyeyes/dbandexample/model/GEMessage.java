package com.silicornio.googlyeyes.dbandexample.model;

import java.util.Calendar;

/**
 * Created by SilicorniO
 */
public class GEMessage {

    private String title;

    private String tag;

    private int numViews;

    private Calendar messageDate;

    private GEMessageText text;

    public GEMessage(String title, GEMessageText text){
        this.title = title;
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getNumViews() {
        return numViews;
    }

    public void setNumViews(int numViews) {
        this.numViews = numViews;
    }

    public Calendar getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(Calendar messageDate) {
        this.messageDate = messageDate;
    }

    public GEMessageText getText() {
        return text;
    }

    public void setText(GEMessageText text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GEMessage geMessage = (GEMessage) o;

        if (!title.equals(geMessage.title)) return false;
        return text != null ? text.equals(geMessage.text) : geMessage.text == null;

    }

    @Override
    public int hashCode() {
        int result = 31 + title.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GEMessage{" +
                "title='" + title + '\'' +
                ", tag='" + tag + '\'' +
                ", numViews=" + numViews +
                ", messageDate=" + messageDate +
                ", text=" + text +
                '}';
    }
}
