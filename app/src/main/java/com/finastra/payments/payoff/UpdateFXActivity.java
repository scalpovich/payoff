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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

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

    }
    private void updateFX(){


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
            parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount() );
        }

    }
    /**
     * This method only converts currency balance to peso
     *
     * @param currency
     * @return
     */
    public Double convertBalanceToPeso(String currency, Double balance) {
        return balance / currency_map.get(currency);
    }

    /**
     * This method only converts currency base from peso, to change foreign to another foreign
     * convert amount to peso first
     *
     * @param currency
     * @return
     */
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

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
