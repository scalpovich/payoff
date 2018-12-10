package com.finastra.payments.payoff;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.OperationCanceledException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.finastra.payments.payoff.db.LedgerEntity;
import com.finastra.payments.payoff.db.PayoffDatabase;
import com.finastra.payments.payoff.db.TransactionType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;


public class SendPaymentActivity extends AppCompatActivity {

    public static final int SERVERPORT = 6000;
    private static final int SOCKET_TIMEOUT = 15000;
    public static final String LOG_INFO = "SendPaymentActivity";

    TextView amountToSend;
    TextView availableBalance;
    String SERVER_IP;

    public String amountToSendStr;

    boolean owner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_payment);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
        Intent intent = getIntent();

        owner = intent.getBooleanExtra("Owner?", false);

        if (intent.hasExtra("Owner Address")) {
            SERVER_IP = intent.getStringExtra("Owner Address");
        }
    }

    public void init() {
        amountToSend = findViewById(R.id.idAmountToSend);
        availableBalance = findViewById(R.id.idCurrentBalance);
        availableBalance.setText(MainActivity.offlineBalanceStr);
    }

    public void onSendPayment(View v) {
        String newBalanceString = "";
        amountToSendStr = amountToSend.getText() + "";
        if (!amountToSendStr.equals("")) {
            String offlineBalanceStr = availableBalance.getText() + "";
            double offlineBalance = Double.parseDouble(offlineBalanceStr);
            double amountToSend = Double.parseDouble(amountToSendStr);
            if (amountToSend > offlineBalance) {
                Context context = getApplicationContext();
                CharSequence text = "Amount to send is greater than the balance";
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(context, text, duration).show();
            } else {
                double newBalance = offlineBalance - amountToSend;
                newBalanceString = newBalance + "";
            }
        } else {
            Context context = getApplicationContext();
            CharSequence text = "Invalid amount";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, text, duration).show();
        }
        MainActivity.offlineBalanceStr = newBalanceString;
        if (Build.VERSION.SDK_INT >= 24) {
            new ClientAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new ClientAsyncTask().execute();
        }
        Log.i(LOG_INFO, "SIZE: "+PayoffDatabase.getInstance().ledgerDao().loadAllLedger().size());
    }

    public class ClientAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            TextView progressTV = findViewById(R.id.progressTV);
            progressTV.setText("I'M CLIENT");
        }

        @Override
        protected String doInBackground(String... params) {
            String hostAddress = SERVER_IP;
            Socket socket = new Socket();
            int port = SERVERPORT;
            PayoffDatabase.getInstance().runInTransaction(() -> {
                try {
                    socket.bind(null);
                    socket.connect((new InetSocketAddress(hostAddress, port)), SOCKET_TIMEOUT);
                    Log.i("CONNECTED", "Connection successful");
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(amountToSendStr.getBytes());
                    PayoffDatabase.getInstance().ledgerDao().insertAll(Arrays.asList(new LedgerEntity("Ron","Rahm", TransactionType.DEBIT.name(), "Sending money", Double.parseDouble(amountToSendStr))));
                    Log.i("SENT TO CLIENT",amountToSendStr);
                } catch (IOException e) {
                    throw new OperationCanceledException("Unable to send transaction");
                } finally {
                    if (socket != null) {
                        if (socket.isConnected()) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(String amountToTransfer) {
            super.onPostExecute(amountToTransfer);
            Intent intent = new Intent(SendPaymentActivity.this,MainActivity.class);
            startActivity(intent);
        }
    }
}