package com.md.cam.dao;

import com.md.cam.entity.Context;
import com.md.cam.utils.Dao;

public class ContextDAO extends Dao<Context> {

    public ContextDAO() {
        super(Context.class);
    }

    @Override
    public void save(Context item) {
        save(item, Context.Sequence_Name);
    }

    public Context findFirst() {
        return (Context) findAll().values().toArray()[0];
    }
}