package com.finastra.payments.payoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.*;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.finastra.payments.wifidirect.WiFiDirectBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Instance variables
    private LinearLayout parentLinearLayout;

    WifiP2pManager wifiP2pManager;
    Channel channel;
    BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;
    ListView discoveredPeersList;
    WifiManager wifiManager;
    WifiP2pManager.PeerListListener peerListListener;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initWifiDirectComponents();

         peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable (WifiP2pDeviceList peerlist) {
               Log.i("Main","PEERS AVAILABLE");
               Log.i("Main",String.valueOf(peerlist.getDeviceList().size()));
                if(!peerlist.getDeviceList().equals(peers)){
                    peers.clear();
                    peers.addAll(peerlist.getDeviceList());
                    deviceNameArray = new String[peerlist.getDeviceList().size()];
                    deviceArray = new WifiP2pDevice[peerlist.getDeviceList().size()];
                    int index = 0;
                    for (WifiP2pDevice device : peerlist.getDeviceList()) {
                        deviceNameArray[index] = device.deviceName;
                        deviceArray[index] = device;
                        index++;
                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View rowView = inflater.inflate(R.layout.field, null);
                        TextView textView = (TextView) rowView;
                        Toast.makeText(getApplicationContext(),"Device Name: "+device.deviceName,Toast.LENGTH_SHORT).show();
                        textView.setText(device.deviceName);
                        View newView = textView;
                        // Add the new row before the add field button.
                        parentLinearLayout.addView(newView, parentLinearLayout.getChildCount() - 1);
                    }
                    /*   ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,deviceNameArray);
                     *//*
                parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount() - 1);*//*
               // Toast.makeText(getApplicationContext(),deviceNameArray,Toast.LENGTH_SHORT).show();
                *//*discoveredPeers*//*
                //parentLinearLayout.setAdapter(adapter);*/
                }
                if (peers.size() == 0) {
                    Toast.makeText(getApplicationContext(),"No device found",Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        };
    }

    public void init() {
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        parentLinearLayout = findViewById(R.id.discoveredPeers);
    }

    public void initWifiDirectComponents() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this,getMainLooper(),null);
        broadcastReceiver = new WiFiDirectBroadcastReceiver(wifiP2pManager,channel,this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.navBankAccount) {
            // Handle the camera action
        } else if (id == R.id.navFxChange) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.navSupport) {

        } else if (id == R.id.navLogut) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onDelete(View v) {
        parentLinearLayout.removeView((View) v.getParent());
    }

    public void onSendMoney(View v) {
        TextView amountToSend = findViewById(R.id.idInputAmount);
        TextView eWalletAmount = findViewById(R.id.eWalletAmount);
        String amountToSendStr = amountToSend.getText() + "";
        String eWalletAmountStr = eWalletAmount.getText() +"";
        double oldValue = Double.parseDouble(eWalletAmountStr);
        double subtractedValue = Double.parseDouble(amountToSendStr);
        View focusView;
        if (subtractedValue > oldValue) {
            Context context = getApplicationContext();
            CharSequence text = "Amount to send is greater than the balance";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, text, duration).show();
        } else {
            double newBalance = oldValue - subtractedValue;
            eWalletAmount.setText(newBalance + "");
        }
    }

    //WiFiDirect

    public void onDiscover(View v) {
        if(wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            wifiP2pManager.discoverPeers(channel,new WifiP2pManager.ActionListener(){
                @Override
                public void onSuccess() {
                    //Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int i) {
                    Toast.makeText(getApplicationContext(), "Discovery starting failed", Toast.LENGTH_SHORT).show();
                    //connectionStatus.setText("Discovery starting failed");
                }
            });
        } else {
            wifiManager.setWifiEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver,intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}
