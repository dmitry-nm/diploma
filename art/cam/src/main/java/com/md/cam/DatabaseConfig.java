package com.md.cam;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.evolve.IncompatibleClassException;

import java.io.File;

public class DatabaseConfig {
    private static DatabaseConfig instance;

    private Environment envmnt;
    private EntityStore store;

    public static DatabaseConfig getInstance() {
        if (instance == null)
            throw new IllegalArgumentException("Database config is notinitialized.");
        return instance;
    }

    public static void init(File envDir) {
        instance = new DatabaseConfig(envDir);
    }

    private DatabaseConfig(File envDir) {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);
        storeConfig.setAllowCreate(true);
        storeConfig.setTransactional(true);
        envmnt = new Environment(envDir, envConfig);
        try {
            store = new EntityStore(envmnt, "autocalc", storeConfig);
        } catch (IncompatibleClassException e) {

        }
    }

    public static void shutdown() {
        if (instance != null) {
            instance.close();
        }
    }

    private void close() {
        store.close();
        envmnt.close();

    }

    public EntityStore getStore() {
        return store;
    }

    public Transaction startTransaction() {
        return envmnt.beginTransaction(null, null);
    }

}