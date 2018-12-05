package com.finastra.payments.payoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.finastra.payments.wifidirect.ClientServerService;
import com.finastra.payments.wifidirect.WiFiDirectBroadcastReceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SendPaymentActivity extends AppCompatActivity
        implements WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

    //Android components
    private LinearLayout parentLinearLayout;
    TextView amountToReceive;
    TextView currentBalance;
    TextView amountToSend;
    TextView availableBalance;
    Button sendButton;

    //Server instance variables
    Thread serverThread = null;
    public static final int SERVERPORT = 6000;
    private static String SERVER_IP;
    Handler updateBalanceHandler;
    SendReceiveAsyncTask sendReceiveAsyncTask;

    //Instance variables
    private boolean owner;

    //WifiDirect variables
    WifiManager wifiManager;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;


    private ServerSocket serverSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        init();
        initWifiDirectComponents();
        updateBalanceHandler = new Handler();
    }

    public void init () {
        parentLinearLayout = findViewById(R.id.discoveredPeers);
        sendButton = parentLinearLayout.findViewById(R.id.btnSendAmount);
        amountToReceive = findViewById(R.id.idAmountToSend);
        currentBalance = findViewById(R.id.idCurrentBalance);
        amountToSend = findViewById(R.id.idAmountToSend);
        availableBalance = findViewById(R.id.idCurrentBalance);
    }


    public void initWifiDirectComponents() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        broadcastReceiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(getApplicationContext(), "Discovery starting failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerlist) {
        if (!peerlist.getDeviceList().equals(peers)) {
            peers.clear();
            peers.addAll(peerlist.getDeviceList());
            deviceNameArray = new String[peerlist.getDeviceList().size()];
            deviceArray = new WifiP2pDevice[peerlist.getDeviceList().size()];
            Log.i("DEVICES NAME: ", String.valueOf(peerlist.getDeviceList().size()));
            int index = 0;
            for (WifiP2pDevice device : peerlist.getDeviceList()) {
                deviceNameArray[index] = device.deviceName;
                deviceArray[index] = device;
                index++;
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.field, null);
                TextView textView = rowView.findViewById(R.id.discoveredPeer);
                textView.setText(device.deviceName);
                textView.setTag(index);
                // Add the new row before the add field button.
                parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount() - 1);
            }
        }
        if (peers.size() == 0) {
            Toast.makeText(getApplicationContext(), "No device found", Toast.LENGTH_SHORT).show();
            return;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
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
        sendReceiveAsyncTask.writeBalance(newBalanceString.getBytes());
/*        Intent intent = new Intent(SendPaymentActivity.this, LoginActivity.class);
        startActivity(intent);*/
    }

    //The information device selected from the list of found devices we have received from existing devicelist'
    //and the configuration information of this device.
    public void onConnectToPeer(View v) {
        Integer position = (Integer) v.getTag();
        Log.i("Item Selected", position + "");
        final WifiP2pDevice deviceToConnect = peers.get(position - 1);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = deviceToConnect.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
            // If the connection is successful, we switch to chat.
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Connected to " + deviceToConnect.deviceName, Toast.LENGTH_SHORT).show();
            }

            //If the connection cannot be established, the error code is printed here.
            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Unable to connect " + reason, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //The method used to retrieve connection information when we connect to a device
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        // When the connection is established, which device is controlled by the group owner. If you own the group we
        // We are sending you to the chat screen, we switch to the chat screen from the home screen.
        // If the other device is a group owner, we will send the address of the group owner to the chat screen.
        Intent intent = getIntent();
        SERVER_IP = wifiP2pInfo.groupOwnerAddress.getHostAddress();
        if (wifiP2pInfo.isGroupOwner) {
            Toast.makeText(getApplicationContext(), "I AM THE SERVER!", Toast.LENGTH_LONG).show();
            new ServerAsyncTask().execute();
        } else {
            Toast.makeText(getApplicationContext(), "I AM THE CLIENT" + wifiP2pInfo.groupOwnerAddress.getHostAddress(), Toast.LENGTH_LONG).show();
            Intent serviceIntent = new Intent(SendPaymentActivity.this,ClientServerService.class);
            serviceIntent.putExtra(ClientServerService.EXTRAS_GROUP_OWNER_ADDRESS, wifiP2pInfo.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(ClientServerService.EXTRAS_GROUP_OWNER_PORT, SERVERPORT);
            SendPaymentActivity.this.startService(serviceIntent);
        }
    }


    public class SendReceiveAsyncTask extends AsyncTask<String,String,Void> {
        private Socket socket;
        private BufferedReader bufferedReaderInput;
        private OutputStream outputStream;

        public SendReceiveAsyncTask(Socket socket) {
            this.socket = socket;
            try {
                this.outputStream = this.socket.getOutputStream();
                this.bufferedReaderInput = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                String read = bufferedReaderInput.readLine();
                new UpdateUIThreadAsyncTask(read).execute();
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

    public class UpdateUIThreadAsyncTask extends AsyncTask<String,String,String> {
        private String newBalance;

        public UpdateUIThreadAsyncTask(String newBalance){
            this.newBalance = newBalance;
        }

        @Override
        protected  String doInBackground(String... params){
            currentBalance.setText(newBalance);
            return newBalance;
        }
    }

    public class ServerAsyncTask extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(SERVERPORT);
                Socket socket = serverSocket.accept();
                sendReceiveAsyncTask = new SendReceiveAsyncTask(socket);
                sendReceiveAsyncTask.execute();
                return "";
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }
    }

}
