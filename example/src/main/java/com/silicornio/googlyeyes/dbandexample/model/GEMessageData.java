package com.silicornio.googlyeyes.dbandexample.model;

/**
 * Created by SilicorniO
 */
public class GEMessageData {

    private String title;
    private String tag;
    private int state;

    private GEMessageText data;

    public GEMessageData(String title){
        this.title = title;
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public GEMessageText getData() {
        return data;
    }

    public void setData(GEMessageText data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "GEMessageData{" +
                "title='" + title + '\'' +
                ", tag='" + tag + '\'' +
                ", state=" + state +
                ", data=" + data +
                '}';
    }
}
