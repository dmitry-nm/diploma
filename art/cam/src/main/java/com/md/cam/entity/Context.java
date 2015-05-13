package com.md.cam.entity;

import com.sleepycat.persist.model.PrimaryKey;

import java.util.Date;

public class Context implements IEntityId<Long> {
    public static final String Sequence_Name = "CO_ID";

    @PrimaryKey(sequence = Sequence_Name)
    private long id;
    private int platform = 0;
    private Date date;

    public Context() {
    }

    public Context(int platform, Date date) {
        super();
        this.platform = platform;
        this.date = date;
    }

    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Context [id=" + id + ", platform=" + platform + ", date="
                + date + "]";
    }
}
