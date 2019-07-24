package com.laurelcrown.payoff;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.laurelcrown.payoff.db.LedgerEntity;
import com.laurelcrown.payoff.db.PayoffDatabase;
import com.laurelcrown.payoff.payoff.R;

import java.util.List;

public class LedgerActivity extends AppCompatActivity {

    private LinearLayout parentLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        parentLinearLayout = findViewById(R.id.ledgerParent);
        uploadTransactions();
    }

    private void uploadTransactions () {
        String ledgerDetails="";
        List<LedgerEntity> loadAllLedger = PayoffDatabase.getInstance().ledgerDao().loadAllLedger();
        PayoffDatabase.getInstance().ledgerDao().loadAllLedger().size();
        for (LedgerEntity name :loadAllLedger) {
            String id = name.getId()+"";
            String amount = name.getAmount()+"";
            String destination = name.getDestination() + "";
            String sender = name.getSender();
            String transactionDetails = name.getTransactionDetails();
            String transactionType = name.getTransactionType();
            ledgerDetails = "AMT: " + amount +" SENDER: " + sender + " DEST: "+ destination + " TXTYPE: " +transactionType;
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewTesting = inflater.inflate(R.layout.field,null);
            TextView textView = viewTesting.findViewById(R.id.discoveredPeer);
            textView.setText(ledgerDetails);
            parentLinearLayout.addView(viewTesting, parentLinearLayout.getChildCount()-1);
        }
    }
}
