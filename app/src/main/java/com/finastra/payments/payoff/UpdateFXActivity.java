package com.finastra.payments.payoff;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.finastra.payments.payoff.db.CurrencyEntity;
import com.finastra.payments.payoff.db.PayoffDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static android.text.TextUtils.isEmpty;

public class UpdateFXActivity extends AppCompatActivity {

    HashMap<String, Double> currency_map = new HashMap<String, Double>();
    private LinearLayout parentLinearLayout;
    private String base;
    private Double BGN;
    private Double CAD;
    private Double BRL;
    private Double HUF;
    private Double DKK;
    private Double JPY;
    private Double ILS;
    private Double TRY;
    private Double RON;
    private Double GBP;
    private Double PHP;
    private Double HRK;
    private Double NOK;
    private Double USD;
    private Double MXN;
    private Double AUD;
    private Double IDR;
    private Double KRW;
    private Double HKD;
    private Double EUR;
    private Double ZAR;
    private Double ISK;
    private Double CZK;
    private Double THB;
    private Double MYR;
    private Double NZD;
    private Double PLN;
    private Double SEK;
    private Double RUB;
    private Double CNY;
    private Double SGD;
    private Double CHF;
    private Double INR;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_current_exchange);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        parentLinearLayout = findViewById(R.id.currencyList);

        new getUpdateExchangeRates().execute();
        updateFX();
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
    private HashMap<String,Double> loadFromDatabase(){
        currency_map = new HashMap<String,Double>();
        List<CurrencyEntity> datalist = PayoffDatabase.getInstance().currencyDao().loadAllCurrency();
        for(CurrencyEntity cE : datalist){
            if(!datalist.contains(cE.getCurrencyName()))
            currency_map.put(cE.getCurrencyName(), cE.getValue());
        }
        return  currency_map;
    }
  
    private void updateFX(){
        currency_map = loadFromDatabase();
        if(currency_map.size() == 0){
            currency_map = defaultValue();
        }
        Iterator it = currency_map.entrySet().iterator();
        for (int i = 0; it.hasNext();i++) {
            HashMap.Entry pair = (HashMap.Entry) it.next();

            if(pair.getKey().equals(base)) {
                continue;
            }
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.content_currency, null);
            View columnView = inflater.inflate(R.layout.content_currency, null);
            TextView fx = columnView.findViewById(R.id.fx);
            TextView rate = columnView.findViewById(R.id.rate);
            fx.setText(pair.getKey() + "");
            rate.setText(""+pair.getValue());
            rowView.setTag(i);
            // Add the new row before the add field button.
            parentLinearLayout.addView(columnView, parentLinearLayout.getChildCount()-1);
        }

    }
    public Double convertBalanceToPeso(String currency, Double balance) {
        return balance / currency_map.get(currency);
    }
    public Double convertToForeignExchangeRate(String currency, Double balance) {
        return balance * currency_map.get(currency);
    }

    public class getUpdateExchangeRates extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL("https://api.exchangeratesapi.io/latest?base=PHP");
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                return buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s!= null && !isEmpty(s)){
                try {
                    JSONObject json = new JSONObject(s);
                    JSONObject jsonObject = json.getJSONObject("rates");
                    base = json.getString("base");
                    BGN = jsonObject.getDouble("BGN");
                    CAD = jsonObject.getDouble("CAD");
                    BRL = jsonObject.getDouble("BRL");
                    HUF = jsonObject.getDouble("HUF");
                    DKK = jsonObject.getDouble("DKK");
                    JPY = jsonObject.getDouble("JPY");
                    ILS = jsonObject.getDouble("ILS");
                    TRY = jsonObject.getDouble("TRY");
                    RON = jsonObject.getDouble("RON");
                    GBP = jsonObject.getDouble("GBP");
                    PHP = jsonObject.getDouble("PHP");
                    HRK = jsonObject.getDouble("HRK");
                    NOK = jsonObject.getDouble("NOK");
                    USD = jsonObject.getDouble("USD");
                    MXN = jsonObject.getDouble("MXN");
                    AUD = jsonObject.getDouble("AUD");
                    IDR = jsonObject.getDouble("IDR");
                    KRW = jsonObject.getDouble("KRW");
                    HKD = jsonObject.getDouble("HKD");
                    EUR = jsonObject.getDouble("EUR");
                    ZAR = jsonObject.getDouble("ZAR");
                    ISK = jsonObject.getDouble("ISK");
                    CZK = jsonObject.getDouble("CZK");
                    THB = jsonObject.getDouble("THB");
                    MYR = jsonObject.getDouble("MYR");
                    NZD = jsonObject.getDouble("NZD");
                    PLN = jsonObject.getDouble("PLN");
                    SEK = jsonObject.getDouble("SEK");
                    RUB = jsonObject.getDouble("RUB");
                    CNY = jsonObject.getDouble("CNY");
                    SGD = jsonObject.getDouble("SGD");
                    CHF = jsonObject.getDouble("CHF");
                    INR = jsonObject.getDouble("INR");

                    currency_map.put("BGN", BGN);
                    currency_map.put("CAD", CAD);
                    currency_map.put("BRL", BRL);
                    currency_map.put("HUF", HUF);
                    currency_map.put("DKK", DKK);
                    currency_map.put("JPY", JPY);
                    currency_map.put("ILS", ILS);
                    currency_map.put("TRY", TRY);
                    currency_map.put("RON", RON);
                    currency_map.put("GBP", GBP);
                    currency_map.put("PHP", PHP);
                    currency_map.put("HRK", HRK);
                    currency_map.put("NOK", NOK);
                    currency_map.put("USD", USD);
                    currency_map.put("MXN", MXN);
                    currency_map.put("AUD", AUD);
                    currency_map.put("IDR", IDR);
                    currency_map.put("KRW", KRW);
                    currency_map.put("HKD", HKD);
                    currency_map.put("EUR", EUR);
                    currency_map.put("ZAR", ZAR);
                    currency_map.put("ISK", ISK);
                    currency_map.put("CZK", CZK);
                    currency_map.put("THB", THB);
                    currency_map.put("MYR", MYR);
                    currency_map.put("NZD", NZD);
                    currency_map.put("PLN", PLN);
                    currency_map.put("SEK", SEK);
                    currency_map.put("RUB", RUB);
                    currency_map.put("CNY", CNY);
                    currency_map.put("SGD", SGD);
                    currency_map.put("CHF", CHF);
                    currency_map.put("INR", INR);
                    updateFX();

                    Iterator it = currency_map.entrySet().iterator();
                    final List<CurrencyEntity> currencyEntities = new ArrayList<CurrencyEntity>();
                    for (int i = 0; it.hasNext(); i++) {
                        HashMap.Entry pair = (HashMap.Entry) it.next();
                        currencyEntities.add(new CurrencyEntity(pair.getKey().toString(), ((Double) pair.getValue()).doubleValue()));
                    }
                    PayoffDatabase.getInstance().runInTransaction(() -> {
                        PayoffDatabase.getInstance().currencyDao().insertAll(currencyEntities);
                    });
                    currency_map.clear();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
