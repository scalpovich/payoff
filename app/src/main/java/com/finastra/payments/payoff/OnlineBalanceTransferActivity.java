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
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


public class OnlineBalanceTransferActivity extends AppCompatActivity {

    TextView tv_amountToWithdraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_balance_transfer);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
    }

    public void init() {
        tv_amountToWithdraw = findViewById(R.id.amountToOnlineBalanceTransfer);
    }

    public void onBalanceOnlineTransfer(View v) {
        String amountToWithdrawStr = tv_amountToWithdraw.getText() + "";
        if (MainActivity.offlineBalanceStr.equals("0.0")){
            Context context = getApplicationContext();
            CharSequence text = "You dont have enough offlineBalance";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, text, duration).show();
        } else {
            double offlineBalance = Double.parseDouble(MainActivity.offlineBalanceStr);
            if (!amountToWithdrawStr.equals("")) {
                double amountToTransfer = Double.parseDouble(amountToWithdrawStr);

                double onlineBalance = Double.parseDouble(AccountActivity.onlineBalanceStr);
                double newOnlineBalanceDbl = onlineBalance + amountToTransfer;
                double newOfflineBalanceDbl = offlineBalance - amountToTransfer;

                String newOfflineBalanceStr = newOfflineBalanceDbl+"";
                String newOnlineBalanceStr = newOnlineBalanceDbl +"";

                AccountActivity.onlineBalanceStr = newOnlineBalanceStr;
                MainActivity.offlineBalanceStr = newOfflineBalanceStr;

                Log.i("ONLINE BALANCE",onlineBalance+"");
                Log.i("AMOUNT TO DEDUCT",amountToTransfer+"");
                Log.i("NEW BAL",newOnlineBalanceStr);

                Intent intent = new Intent(this,AccountActivity.class);
                intent.putExtra("newBalance",newOnlineBalanceStr);
                startActivity(intent);
            } else {
                Context context = getApplicationContext();
                CharSequence text = "Invalid amount";
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(context, text, duration).show();
            }
        }

    }

}
