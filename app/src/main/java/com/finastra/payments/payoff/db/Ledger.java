package com.finastra.payments.payoff.db;

import java.math.BigInteger;

public interface Ledger {
    int getId();
    String getSender();
    String getDestination();
    String getTransactionType();
    String getTransactionDetails();
    double getAmount();
}
