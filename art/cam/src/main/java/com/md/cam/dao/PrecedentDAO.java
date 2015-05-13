package com.md.cam.dao;

import com.md.cam.entity.Precedent;
import com.md.cam.utils.Dao;

public class PrecedentDAO extends Dao<Precedent> {

    public PrecedentDAO() {
        super(Precedent.class);
    }

    @Override
    public void save(Precedent item) {
        save(item, Precedent.Sequence_Name);
    }
}