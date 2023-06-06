package com.tinieblas.pasarela;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Header;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stripe.Stripe;
import com.stripe.android.PaymentConfiguration;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Payout;
import com.stripe.net.RequestOptions;
import com.stripe.android.paymentsheet.*;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String apiKeyPublica = "pk_test_51K1HxzInA4seMnObTZKEglgrDGloAeK7gZQcLMVHxKduCVKYjnUVc8hbspGDxbMBtZZlqu9cWOAPcIqLjJ4l6rf100bsncJeKk";
    String apiKeySecreta = "sk_test_51K1HxzInA4seMnObqljmh8wtPw2tS3RTIOYGdMIehahSp2cA3m1wDiYBtzt2JUtqghHwM4fz0idI74PoNIURAKjA00T1XLINJ8";
    PaymentSheet paymentSheet;
    String customer_ID;
    String EphericalKey;
    String ClienteSecret;
    TextView texto;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        texto = findViewById(R.id.texttoSiPago);

        PaymentConfiguration.init(this, apiKeyPublica);
        paymentSheet = new PaymentSheet(this, paymentSheetResult -> {
            onPaymentResult(paymentSheetResult);

        });

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/customers",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            customer_ID = jsonObject.getString("id");
                            getEphericalKey(customer_ID);
                            //Toast.makeText(MainActivity.this, customer_ID, Toast.LENGTH_SHORT).show();

                            Log.e("===> customer_ID", customer_ID);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            public Map<String, String> getHeaders() throws AuthFailureError{
                Map<String, String> headStringStringMap = new HashMap<>();
                headStringStringMap.put("Authorization", "Bearer " + apiKeySecreta);
                return headStringStringMap;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);

    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if(paymentSheetResult instanceof PaymentSheetResult.Completed){
            Log.e("==> ", "payment success");
        }
    }

    private void getEphericalKey(String customer_ID){
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/ephemeral_keys",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            EphericalKey = jsonObject.getString("id");

                            getClientSecret(customer_ID, EphericalKey);
                            //Toast.makeText(MainActivity.this, EphericalKey, Toast.LENGTH_SHORT).show();
                            Log.e("==> EphericalKey", EphericalKey);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            public Map<String, String> getHeaders() throws AuthFailureError{
                Map<String, String> headStringStringMap = new HashMap<>();
                headStringStringMap.put("Authorization", "Bearer " + apiKeySecreta);
                headStringStringMap.put("Stripe-Version", "2020-08-27");

                return headStringStringMap;
            }
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer" ,  customer_ID);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
    }

    private void getClientSecret(String customer_id, String ephericalKey) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/payment_intents",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            ClienteSecret = jsonObject.getString("client_secret");
                            PaymentFlow();
                            //Toast.makeText(MainActivity.this, ClienteSecret, Toast.LENGTH_SHORT).show();
                            //getClientSecret(customer_ID, EphericalKey);
                            Log.e("==> ClientSecret", ClienteSecret);

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            public Map<String, String> getHeaders() throws AuthFailureError{
                Map<String, String> headStringStringMap = new HashMap<>();
                headStringStringMap.put("Authorization", "Bearer " + apiKeySecreta);
                return headStringStringMap;
            }
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer" ,  customer_id);
                params.put("amount", "1000" + 00);
                params.put("currency", "usd");
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
    }

    private void PaymentFlow() {
        paymentSheet.presentWithPaymentIntent(
                ClienteSecret, new PaymentSheet.Configuration("ABC Company",
                        new PaymentSheet.CustomerConfiguration(
                                customer_ID,
                                EphericalKey
                        ))
        );
    }

    public void PayYou(View view){
        // Ejecutar las operaciones de red en un AsyncTask
        //new NetworkTask().execute();
    }

    @Override
    public void onClick(View view) {

    }
/*
    private class NetworkTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            RequestOptions requestOptions = RequestOptions.builder()
                    .setApiKey("sk_test_51K1HxzInA4seMnObqljmh8wtPw2tS3RTIOYGdMIehahSp2cA3m1wDiYBtzt2JUtqghHwM4fz0idI74PoNIURAKjA00T1XLINJ8")
                    .build();

            try {
                Customer customer = Customer.retrieve(
                        "cus_LvDREDHT7HwI7r",
                        requestOptions);
            } catch (StripeException e) {
                throw new RuntimeException(e);
            }

            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Void aVoid) {
            // Aquí puedes realizar cualquier actualización de la interfaz de usuario necesaria después de completar las operaciones de red

            //texto.setText("Pago Realizado con exito");
            Stripe.apiKey = "sk_test_51K1HxzInA4seMnObqljmh8wtPw2tS3RTIOYGdMIehahSp2cA3m1wDiYBtzt2JUtqghHwM4fz0idI74PoNIURAKjA00T1XLINJ8";

            Map<String, Object> params = new HashMap<>();
            params.put("amount", 1100);
            params.put("currency", "mxn");

            try {
                Payout payout = Payout.create(params);
                System.out.println(payout);
            } catch (StripeException e) {
                throw new RuntimeException(e);
            }

        }
    }*/
}



















































