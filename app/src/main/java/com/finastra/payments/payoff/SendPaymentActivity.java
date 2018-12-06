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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class SendPaymentActivity extends AppCompatActivity {

    public static final int SERVERPORT = 6000;
    private static final int SOCKET_TIMEOUT = 5000;

    private ServerSocket serverSocket;

    TextView amountToSend;
    TextView availableBalance;
    String SERVER_IP;

    SendReceiveAsyncTask sendReceiveAsyncTask;

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

        if(intent.hasExtra("Owner Address")) {
            SERVER_IP = intent.getStringExtra("Owner Address");
        }

        if (owner) {
            if(Build.VERSION.SDK_INT >= 24) {
                new ServerAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                new ServerAsyncTask().execute();
            }
        } else {
            if(Build.VERSION.SDK_INT >= 24) {
                new ClientAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                new ClientAsyncTask().execute();

            }
        }
    }

    public void init() {
        amountToSend = findViewById(R.id.idAmountToSend);
        availableBalance = findViewById(R.id.idCurrentBalance);
        availableBalance.setText(MainActivity.offlineBalanceStr);
    }

    public void onSendPayment(View v){
        String newBalanceString = "";
        String amountToSendStr = amountToSend.getText() + "";
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
        if(serverSocket != null) {
            sendReceiveAsyncTask.writeBalance(newBalanceString.getBytes());
        } else {
            Log.i("SERVERSOCKET","IS NULL");
        }
    }

    public class UpdateUIThreadAsyncTask extends AsyncTask<String,String,String> {
        private String newBalance;

        public UpdateUIThreadAsyncTask(String newBalance){
            this.newBalance = newBalance;
        }

        @Override
        protected  String doInBackground(String... params){
            MainActivity.offlineBalanceStr = newBalance;
            return newBalance;
        }

        @Override
        protected  void onPreExecute() {
            super.onPreExecute();
            TextView progressTV = findViewById(R.id.progressTV);
            progressTV.setText("Setting the balance socket");
        }
    }

    public class ServerAsyncTask extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
                socket = serverSocket.accept();
                sendReceiveAsyncTask = new SendReceiveAsyncTask(socket);
                if(Build.VERSION.SDK_INT >= 24) {
                    Log.i("GREATER THAN VERSION","NOUGAT AND ABOVE");
                    sendReceiveAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    Log.i("NOT GREATER","NOUGAT");
                    sendReceiveAsyncTask.execute();
                }
                Log.i("SOCKET CREATED","socket created");
                return "";
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected  void onPreExecute() {
            super.onPreExecute();
            TextView progressTV = findViewById(R.id.progressTV);
            progressTV.setText("IM SERVER");
        }
    }

    public class ClientAsyncTask extends AsyncTask<String,String,String> {
        @Override
        protected  String doInBackground (String... params) {
            String hostAddress = SERVER_IP;
            Socket socket = new Socket();
            int port = SERVERPORT;
            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(hostAddress, port)), SOCKET_TIMEOUT);
                Log.i("CONNECTED", "Connection successful");
            } catch (IOException e) {
                e.printStackTrace();
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
            return null;
        }

        @Override
        protected  void onPreExecute() {
            super.onPreExecute();
            TextView progressTV = findViewById(R.id.progressTV);
            progressTV.setText("I'M CLIENT");
        }
    }


    public class SendReceiveAsyncTask extends AsyncTask<String,String,Void> {
        private Socket socket;
        private BufferedReader bufferedReaderInput;
        private OutputStream outputStream;

        public SendReceiveAsyncTask(Socket socket) {
            this.socket = socket;
            try {
                Log.i("SENDRECEIVE","intializing");
                this.outputStream = this.socket.getOutputStream();
                this.bufferedReaderInput = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                Log.i("SENDRECEIVE","executing");
                String read = bufferedReaderInput.readLine();
                if(Build.VERSION.SDK_INT >= 24) {
                    new UpdateUIThreadAsyncTask(read).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    new UpdateUIThreadAsyncTask(read).execute();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void writeBalance(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
