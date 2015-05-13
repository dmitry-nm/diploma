package com.md.cam.entity;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import java.util.Date;

@Entity
public class LogTime implements IEntityId<Long> {
    public static final String Sequence_Name = "LT_ID";

    @PrimaryKey(sequence = Sequence_Name)
    private long id;
    private double time;
    private Date date = new Date();

    public LogTime() {
    }

    public LogTime(double time) {
        super();
        this.time = time;
    }

    public LogTime(long id, double time) {
        super();
        this.id = id;
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "LogTime [id=" + id + ", time=" + time + ", date=" + date + "]";
    }
}
