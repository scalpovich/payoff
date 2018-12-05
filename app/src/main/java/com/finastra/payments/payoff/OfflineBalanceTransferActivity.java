package com.finastra.payments.payoff;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class OfflineBalanceTransferActivity extends AppCompatActivity {

    TextView tv_amountToTransfer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_ewallet);
        init();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void init() {
       tv_amountToTransfer = findViewById(R.id.amountToOfflineBalanceTransfer);
    }

    public void onBalanceOfflineTransfer(View v) {
        double onlineBalance = Double.parseDouble(AccountActivity.onlineBalanceStr);
        String amountToTransferStr = tv_amountToTransfer.getText() + "";

        if (!amountToTransferStr.equals("")) {
            double amountToTransfer = Double.parseDouble(amountToTransferStr);
            double newBalance = onlineBalance - amountToTransfer;
            double offlineBalance = Double.parseDouble(MainActivity.offlineBalanceStr);
            double newOfflineBalanceDbl = offlineBalance + amountToTransfer;

            String newOfflineBalance = newOfflineBalanceDbl +"";

            MainActivity.offlineBalanceStr = newOfflineBalance;

            String newBalanceStr = newBalance +"";

            Log.i("ONLINE BALANCE",onlineBalance+"");
            Log.i("AMOUNT TO DEDUCT",amountToTransfer+"");
            Log.i("NEW BAL",newBalanceStr);

            Intent intent = new Intent(this,AccountActivity.class);
            intent.putExtra("newBalance",newBalanceStr);
            startActivity(intent);
        } else {
            Context context = getApplicationContext();
            CharSequence text = "Invalid amount";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, text, duration).show();
        }
    }
}
