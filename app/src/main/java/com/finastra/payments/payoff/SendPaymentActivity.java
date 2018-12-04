package com.finastra.payments.payoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.finastra.payments.wifidirect.WiFiDirectBroadcastReceiver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class SendPaymentActivity extends AppCompatActivity
        implements WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

    //Android components
    private LinearLayout parentLinearLayout;
    TextView amountToReceive;
    TextView currentBalance;


    //Server instance variables
    private ServerSocket serverSocket;
    Handler updateBalanceHandler;
    Thread serverThread = null;
    public static final int SERVERPORT = 6000;
    private static String SERVER_IP;
    private Socket socket;

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        init();
        initWifiDirectComponents();

        Intent intent = getIntent();

        owner = intent.getBooleanExtra("Owner?", false);
        SERVER_IP = intent.getStringExtra("Owner Address");
        amountToReceive = findViewById(R.id.idAmountToSend);
        currentBalance = findViewById(R.id.homeCurrentBal);
        updateBalanceHandler = new Handler();

        Log.i("CURRRENT BAL",currentBalance.getText().toString());
        Log.i("SERVER_IP",SERVER_IP);

        if (owner) {
            this.serverThread = new Thread(new ServerThread());
            this.serverThread.start();
        } else {
            new Thread(new ClientThread()).start();
        }
    }

    public void init () {
        parentLinearLayout = findViewById(R.id.discoveredPeers);
    }


    public void onSendPayment(View v) {
        TextView amountToSend = findViewById(R.id.idAmountToSend);
        TextView availableBalance = findViewById(R.id.idCurrentBalance);
        String amountToSendStr = amountToSend.getText() + "";
        String newBalanceString = "";
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
                availableBalance.setText(newBalanceString);
            }
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream()
                        )), true);
                out.println(newBalanceString);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Context context = getApplicationContext();
            CharSequence text = "Invalid amount";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, text, duration).show();
        }
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


    //The information device selected from the list of found devices we have received from existing devicelist'
    //and the configuration information of this device.
    public void onConnectToPeer(View v) {
        Integer position = (Integer) v.getTag();
        Log.i("Item Selected", position + "");
        final WifiP2pDevice deviceToConnect = peers.get(position - 1);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = deviceToConnect.deviceAddress;
        // config.groupOwnerIntent = 15;
        wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
            // If the connection is successful, we switch to chat.
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Successfully connected to " + deviceToConnect.deviceName, Toast.LENGTH_SHORT).show();
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
        if (wifiP2pInfo.isGroupOwner) {
            Toast.makeText(getApplicationContext(), "Yay I am the owner host!!", Toast.LENGTH_LONG).show();
            // Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
            //chatIntent.putExtra("Owner?",true); //Grup sahibi benim!
            //MainActivity.this.startActivity(chatIntent);
        } else {
            Toast.makeText(getApplicationContext(), "The owner is client: " +
                    wifiP2pInfo.groupOwnerAddress.getHostAddress(), Toast.LENGTH_LONG).show();
            //Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
            //chatIntent.putExtra("Owner?",false); //Grup sahibi diğer cihaz :(
            ///chatIntent.putExtra("Owner Address", wifiP2pInfo.groupOwnerAddress.getHostAddress());
            // MainActivity.this.startActivity(chatIntent);
        }
    }

    class ServerThread implements Runnable {
        @Override
        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    CommunicationThread communicationThread = new CommunicationThread(socket);
                    new Thread(communicationThread).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    updateBalanceHandler.post(new UpdateUIThread(read));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class UpdateUIThread implements Runnable {
        private String newBalance;

        public UpdateUIThread(String newBalance) {
            this.newBalance = newBalance;
        }

        @Override
        public void run() {
            currentBalance.setText(newBalance);
        }
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddress, SERVERPORT);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
