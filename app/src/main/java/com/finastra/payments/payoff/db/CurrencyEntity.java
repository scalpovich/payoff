package com.finastra.payments.payoff.db;

import android.arch.persistence.room.Entity;

import javax.annotation.PropertyKey;

@Entity(tableName = "currency")
public class CurrencyEntity implements Currency{

    @PropertyKey
    private String currencyName;
    private double value;

    @Override
    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    @Override
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
