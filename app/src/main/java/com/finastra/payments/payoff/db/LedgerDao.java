package com.finastra.payments.payoff.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface LedgerDao {
    @Query("SELECT * FROM ledger")
    LiveData<List<LedgerEntity>> loadAllLedger();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LedgerEntity> ledgers);

    @Query("select * from ledger where transactionType = :transactionType")
    LiveData<List<LedgerEntity>> loadLedgerByType(String transactionType);

}
