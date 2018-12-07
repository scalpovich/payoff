package com.finastra.payments.payoff;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
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


public class ReceivePaymentActivity extends AppCompatActivity {

    String SERVER_IP;
    TextView amountToSend;
    boolean owner;

    public static final String LOG_INFO = "ReceivePaymentActivity";
    public static final int SERVERPORT = 6000;
    private static final int SOCKET_TIMEOUT = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_payment);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= 24) {
            new ServerAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new ServerAsyncTask().execute();
        }
    }

    public class ServerAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(SERVERPORT);
                Log.i(LOG_INFO, "ServerAsyncTask: Socket opened");
                Socket client = serverSocket.accept();
                Log.i(LOG_INFO, "ServerAsyncTask: connection done");
                InputStream inputstream = client.getInputStream();
                String amountToTransfer = getStringFromInputStream(inputstream);
                Log.i(LOG_INFO, "RECEIVED FROM CLIENT: " + amountToTransfer);
                serverSocket.close();
                return amountToTransfer;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String amountToTransfer) {
            super.onPostExecute(amountToTransfer);
            Intent intent = new Intent(ReceivePaymentActivity.this,MainActivity.class);
            intent.putExtra("amountTransferred",amountToTransfer);
            startActivity(intent);
        }
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
}
