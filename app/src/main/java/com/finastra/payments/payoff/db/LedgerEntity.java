package com.finastra.payments.payoff.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.math.BigInteger;

@Entity(tableName = "ledger")
public class LedgerEntity implements Ledger {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String transactionType;
    private String transactionDetails;
    private double amount;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public String getTransactionDetails() {
        return transactionDetails;
    }

    public void setTransactionDetails(String transactionDetails) {
        this.transactionDetails = transactionDetails;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LedgerEntity() {
    }

    @Ignore
    public LedgerEntity(String transactionType, String transactionDetails, double amount) {
        this.transactionType = transactionType;
        this.transactionDetails = transactionDetails;
        this.amount = amount;
    }

    public LedgerEntity(Ledger ledger) {
        this.id = ledger.getId();
        this.transactionType = ledger.getTransactionType();
        this.transactionDetails = ledger.getTransactionDetails();
        this.amount = ledger.getAmount();
    }
}
