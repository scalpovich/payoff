package com.finastra.payments.payoff;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.finastra.payments.payoff.db.CurrencyEntity;
import com.finastra.payments.payoff.db.PayoffDatabase;

import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    WifiManager wifiManager;
    public static String offlineBalanceStr="0.0";
    TextView offlineBalance;
    public static String btnPressed;
    HashMap<String,Double> currency_map;
    private String defaultCurrency="PHP";
    private Double valuePHP=0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PayoffDatabase.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        init();
        enableWifi();
        currency_map = loadCurrencyFromDatabase();
        currency_map = (currency_map == null || currency_map.size()==0) ? defaultValue(): currency_map ;
    }

    public void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();

        offlineBalance = findViewById(R.id.homeOfflineBalance);
        offlineBalance.setText(offlineBalanceStr);

        if(intent.hasExtra("amountTransferred")) {
            double offlineBalanceDbl = Double.parseDouble(offlineBalanceStr);
            double amountTransferred = Double.parseDouble(intent.getExtras().getString("amountTransferred"));
            double newOfflineBalance = offlineBalanceDbl+amountTransferred;
            offlineBalanceStr = newOfflineBalance +"";
            offlineBalance.setText(offlineBalanceStr);
        }

        //Foreign currencies
        Spinner spinner = findViewById(R.id.spr_currencies);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String selectedItem = parent.getItemAtPosition(position).toString();
               handleSpinnerSelect(selectedItem);
            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        //Side Menu
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    public void enableWifi() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
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
            Intent intent = new Intent (MainActivity.this,AccountActivity.class);
            startActivity(intent);
        } else if (id == R.id.navFxChange) {
            Intent intent = new Intent(MainActivity.this,UpdateFXActivity.class);
            startActivity(intent);
        } else if (id == R.id.navTransactionHistory) {
            Intent intent = new Intent(MainActivity.this, LedgerActivity.class);
            startActivity(intent);
        } else if (id == R.id.navSupport) {

        } else if (id == R.id.navLogut) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            wifiManager.setWifiEnabled(false);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void onDiscoverPeers(View v) {
        btnPressed = v.getTag() +"";
        Intent intent = new Intent(MainActivity.this, ConnectToPeersActivity.class);
        startActivity(intent);
    }

    private HashMap<String, Double> loadCurrencyFromDatabase(){
        PayoffDatabase.getInstance().currencyDao().loadAllCurrency();
        currency_map = new HashMap<String,Double>();
        List<CurrencyEntity> datalist = PayoffDatabase.getInstance().currencyDao().loadAllCurrency();
        for(CurrencyEntity cE : datalist){
            if(!datalist.contains(cE.getCurrencyName()))
                currency_map.put(cE.getCurrencyName(), cE.getValue());
        }
        return  currency_map;
    }

    private HashMap<String, Double> defaultValue(){
        currency_map = new HashMap<String,Double>();
        currency_map.put("BGN",0.0326630816);
        currency_map.put("CAD",0.0251778617);
        currency_map.put("BRL",0.072968369);
        currency_map.put("HUF",5.4024850529);
        currency_map.put("DKK",0.1246367614);
        currency_map.put("JPY",2.1428571429);
        currency_map.put("ILS",0.0706319516);
        currency_map.put("TRY",0.1009602859);
        currency_map.put("RON",0.0777046662);
        currency_map.put("GBP",0.0148443502);
        currency_map.put("PHP",1.0);
        currency_map.put("HRK",0.1235679214);
        currency_map.put("NOK",0.1611276262);
        currency_map.put("ZAR",0.2611409867);
        currency_map.put("MXN",0.3879254484);
        currency_map.put("AUD",0.0260012024);
        currency_map.put("USD",0.0189618892);
        currency_map.put("KRW",21.1161027422);
        currency_map.put("HKD",0.1481261899);
        currency_map.put("EUR",0.0167006246);
        currency_map.put("ISK",2.3280670697);
        currency_map.put("CZK",0.4323123685);
        currency_map.put("THB",0.621096229);
        currency_map.put("MYR",0.0787684959);
        currency_map.put("NZD",0.0273856842);
        currency_map.put("PLN",0.0715220949);
        currency_map.put("CHF",0.0189184676);
        currency_map.put("SEK",0.1699338655);
        currency_map.put("CNY",0.1300110224);
        currency_map.put("SGD",0.0259193694);
        currency_map.put("INR",1.3371522095);
        currency_map.put("IDR",272.9563445673);
        currency_map.put("RUB",1.266550319);
        return  currency_map;
    }

    private void handleSpinnerSelect(String selected) {
        Double offlineBalance = Double.parseDouble(offlineBalanceStr);
        Log.i("OFFLINEBALANCE",offlineBalance+"");
        Double convertedOfflineBalance =0.0;
        TextView offlineBalanceView = findViewById(R.id.homeOfflineBalance);
        if (offlineBalance <= 0.0) {
            return;
        }
        if(selected.equals(defaultCurrency)) return;

        if(selected.equals("PHP")){ //if converting to PHP:
            convertedOfflineBalance =  offlineBalance / currency_map.get(defaultCurrency);
            defaultCurrency = "PHP";
            //if  converting from php:
        } else if(defaultCurrency.equals("PHP")){ //if converting from PHP:
            convertedOfflineBalance = offlineBalance * currency_map.get(selected);
            defaultCurrency = selected;

        } else{ // if converting from foreign to another foreign exchange: convert to Peso first
            offlineBalance /= currency_map.get(defaultCurrency);
            convertedOfflineBalance = offlineBalance * currency_map.get(selected);
            defaultCurrency =selected;
        }
        offlineBalanceStr = convertedOfflineBalance+"";
        Log.i("CONVERTED",convertedOfflineBalance+"");
        offlineBalanceView.setText(convertedOfflineBalance+"");

    }
}
