package com.finastra.payments.payoff.db;

import java.math.BigInteger;

public interface Ledger {
    int getId();
    String getTransactionType();
    String getTransactionDetails();
    double getAmount();
}
