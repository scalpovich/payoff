package com.laurelcrown.payoff.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface CurrencyDao {
    @Query("SELECT * FROM currency")
    List<CurrencyEntity> loadAllCurrency();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CurrencyEntity> currencies);

    @Query("select * from currency where currencyName = :currencyName")
    LiveData<CurrencyEntity> loadCurrencyByName(String currencyName);

}
