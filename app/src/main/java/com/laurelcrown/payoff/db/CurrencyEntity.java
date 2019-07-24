package com.laurelcrown.payoff.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import android.support.annotation.NonNull;

@Entity(tableName = "currency")
public class CurrencyEntity implements Currency{

    @PrimaryKey
    @NonNull private String currencyName;
    private double value;

    @Override
    @NonNull
    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(@NonNull String currencyName) {
        this.currencyName = currencyName;
    }

    @Override
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public CurrencyEntity(@NonNull String currencyName, double value) {
        this.currencyName = currencyName;
        this.value = value;
    }

    public CurrencyEntity(Currency currency) {
        this.currencyName = currency.getCurrencyName();
        this.value = currency.getValue();
    }
}
