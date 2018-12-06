package com.finastra.payments.payoff;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class SendPaymentActivity extends AppCompatActivity {

    public static final int SERVERPORT = 6000;
    private static final int SOCKET_TIMEOUT = 5000;
    TextView amountToSend;
    TextView availableBalance;
    String SERVER_IP;

    public String amountToSendStr;

    boolean owner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_payment);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
        Intent intent = getIntent();

        owner = intent.getBooleanExtra("Owner?", false);

        if (intent.hasExtra("Owner Address")) {
            SERVER_IP = intent.getStringExtra("Owner Address");
        }

        if (owner) {
            if (Build.VERSION.SDK_INT >= 24) {
                new ServerAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                new ServerAsyncTask().execute();
            }
        } else {
            TextView progressTV = findViewById(R.id.progressTV);
            progressTV.setText("I'M CLIENT");
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
            String eWalletAmountStr = availableBalance.getText() + "";
            double oldValue = Double.parseDouble(eWalletAmountStr);
            double subtractedValue = Double.parseDouble(amountToSendStr);
            if (subtractedValue > oldValue) {
                Context context = getApplicationContext();
                CharSequence text = "Amount to send is greater than the balance";
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(context, text, duration).show();
            } else {
                double newBalance = oldValue - subtractedValue;
                newBalanceString = newBalance + "";
            }
        } else {
            Context context = getApplicationContext();
            CharSequence text = "Invalid amount";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, text, duration).show();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            new ClientAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new ClientAsyncTask().execute();
        }
    }

    public class ServerAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                ServerSocket serverSocket = new ServerSocket(SERVERPORT);
                Log.d("ServerAsyncTask: ", "Socket opened");
                Socket client = serverSocket.accept();
                Log.d("ServerAsyncTask: ", "connection done");
                InputStream inputstream = client.getInputStream();
                Log.d("RECEIVED from CLIENT: ", getStringFromInputStream(inputstream));
                return "";
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            TextView progressTV = findViewById(R.id.progressTV);
            progressTV.setText("IM SERVER");
        }
/*
@Override
protected void onPostExecute(String s) {
super.onPostExecute(s);
double receiverBalance = Double.parseDouble(MainActivity.offlineBalanceStr);
double receivedAmount = Double.parseDouble(s);
double newBalance = receiverBalance + receivedAmount;
String newBalanceSTr = newBalance + "";
Log.i("ServerAsyncTask",newBalanceSTr);
MainActivity.offlineBalanceStr = newBalanceSTr;
}*/
    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }


    public class ClientAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String hostAddress = SERVER_IP;

            Socket socket = new Socket();
            int port = SERVERPORT;
            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(hostAddress, port)), SOCKET_TIMEOUT);
                Log.i("CONNECTED", "Connection successful");
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(amountToSendStr.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            TextView progressTV = findViewById(R.id.progressTV);
            progressTV.setText("I'M CLIENT");
        }
    }
}