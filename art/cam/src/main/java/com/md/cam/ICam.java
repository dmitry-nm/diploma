package com.md.cam;

import java.io.File;

public interface ICam {
    public static final File pathDb = new File(android.os.Environment.getExternalStorageDirectory(), "cam");
    public boolean calcOnClient();
}
