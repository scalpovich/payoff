package com.laurelcrown.payoff.payoff;

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.laurelcrown.payoff.db.LedgerDao;
import com.laurelcrown.payoff.db.LedgerEntity;
import com.laurelcrown.payoff.db.PayoffDatabase;
import com.laurelcrown.payoff.db.TransactionType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class LedgerDaoTest {

    static final LedgerEntity CREDIT_TRANSACTION = new LedgerEntity("Tamy", "Rahm",  TransactionType.CREDIT.name(), "Show me the money!!!",
            500d);

    static final LedgerEntity DEBIT_TRANSACTION = new LedgerEntity("Ron", "Edsel", TransactionType.DEBIT.name(), "Don't take it away!!!",
            200d);

    static final List<LedgerEntity> TRANSACTIONS = Arrays.asList(CREDIT_TRANSACTION, DEBIT_TRANSACTION);

    private PayoffDatabase payoffDatabase;
    private LedgerDao ledgerDao;

    @Before
    public void init() {
        payoffDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getTargetContext(), PayoffDatabase.class)
                .allowMainThreadQueries()
                .build();
        ledgerDao = payoffDatabase.ledgerDao();
    }

    @After
    public void teardown() {
        payoffDatabase.close();
    }

    @Test
    public void dbIsInitialized() {
        Assert.assertNotNull(payoffDatabase);
    }

    @Test
    public void insertRecords() throws InterruptedException {
        ledgerDao.insertAll(TRANSACTIONS);

//        Assert.assertThat(LiveDataTestUtil.getValue(ledgerDao.loadAllLedger()).size(), is(2));
    }

    @Test
    public void getByTransactionType() throws InterruptedException {
        ledgerDao.insertAll(TRANSACTIONS);
//        Assert.assertThat(LiveDataTestUtil.getValue(ledgerDao.loadLedgerByType(TransactionType.CREDIT.name())).size(), is(1));
    }
}
