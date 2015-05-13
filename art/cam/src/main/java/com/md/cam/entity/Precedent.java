package com.md.cam.entity;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class Precedent implements IEntityId<Long> {
    public static final String Sequence_Name = "PR_ID";

    @PrimaryKey(sequence = Sequence_Name)
    private long id;
    private double result;
    private double[] params;

    public Precedent() {
    }

    public Precedent(double result, double... params) {
        super();
        this.result = result;
        this.params = params;
    }

    public Precedent(long id, double result, double... params) {
        super();
        this.id = id;
        this.result = result;
        this.params = params;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }

    public double[] getParams() {
        return params;
    }

    public void setParams(double[] params) {
        this.params = params;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for(double p:params) {
            str.append(p);
            str.append(", ");
        }
        return "Predicate [id=" + id + ", result=" + result + ", params="
                + str.toString() + "]";
    }
}
