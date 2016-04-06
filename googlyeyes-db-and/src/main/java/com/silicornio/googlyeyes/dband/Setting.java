package com.silicornio.googlyeyes.dband;

public class Setting {

    private String name;

    private String value;

    private String value2;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    @Override
    public String toString() {
        return "Setting{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", value2='" + value2 + '\'' +
                '}';
    }
}
