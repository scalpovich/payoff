package com.finastra.payments.payoff.db;

import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = LedgerEntity.class, version = 1)
public abstract class PayoffDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "payoff-db";
    public abstract LedgerDao ledgerDao();

    private static PayoffDatabase INSTANCE;

    private final MutableLiveData<Boolean> isDbCreated = new MutableLiveData<>();

    public static PayoffDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (PayoffDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context, PayoffDatabase.class, DATABASE_NAME)
                            .allowMainThreadQueries()
                            .build();
                    INSTANCE.updateDatabaseState(context);
                }
            }
        }
        return INSTANCE;
    }

    private void updateDatabaseState(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreatedState();
        }
    }

    private void setDatabaseCreatedState() {
        isDbCreated.postValue(true);
    }
}
