package com.md.art.pojo;

/**
 * Created by MD on 10.05.2015.
 */
public class Rating {
    private String name;
    private float value;
    private float max_value;

    public Rating()
    {

    }

    public Rating(String name,float value,float max_value)
    {
        this.name=name;
        this.value=value;
        this.max_value=max_value;
    }

    @Override
    public String toString()
    {
        return name+":"+value+"/"+max_value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getMax_value() {
        return max_value;
    }

    public void setMax_value(float max_value) {
        this.max_value = max_value;
    }
}
