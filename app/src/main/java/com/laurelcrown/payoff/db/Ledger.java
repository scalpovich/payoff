package com.laurelcrown.payoff.db;

public interface Ledger {
    int getId();
    String getSender();
    String getDestination();
    String getTransactionType();
    String getTransactionDetails();
    double getAmount();
}
