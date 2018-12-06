package com.finastra.payments.payoff.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = LedgerEntity.class, version = 1)
public abstract class PayoffDatabase extends RoomDatabase {
    public abstract LedgerDao ledgerDao();
}
