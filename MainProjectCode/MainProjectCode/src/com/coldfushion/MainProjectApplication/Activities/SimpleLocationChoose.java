package com.coldfushion.MainProjectApplication.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;
import android.os.Handler;

import com.coldfushion.MainProjectApplication.Helpers.MyJavaScriptInterface;
import com.coldfushion.MainProjectApplication.Helpers.Network;
import com.coldfushion.MainProjectApplication.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Kraaijeveld on 8-6-2015.
 */
public class SimpleLocationChoose extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    Network network;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locationchoose_layout);

        network = new Network(getApplicationContext());

        if (network.isOnline()){
        webview = (WebView) findViewById(R.id.WebviewLocationChoose);
        webview.getSettings().setJavaScriptEnabled(true);

        webview.addJavascriptInterface(myjavascriptinterface, "HTMLViewer");

        webview.loadUrl("http://school.ceesjannolen.nl/app/index.html");

        //googleapiclient
        mGoogleApiClient = new GoogleApiClient.Builder(SimpleLocationChoose.this).addApi(Places.GEO_DATA_API).addConnectionCallbacks(this).build();
        mGoogleApiClient.connect();}
        else {
            Toast t = new Toast(getApplicationContext());
            t.makeText(getApplicationContext(), "Geen internet verbinding beschikbaar", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

        //VARS
        Boolean latlngset = false;
        WebView webview;
        MyJavaScriptInterface myjavascriptinterface = new MyJavaScriptInterface(this);
        GoogleApiClient mGoogleApiClient;
        private LatLng newlatlng;
        String placeId;
        String placeAdress;
        ProgressDialog pDialog;

    //location choose event click
    public void ChooseLocation(View view) {

        pDialog = new ProgressDialog(SimpleLocationChoose.this);
        pDialog.setMessage("Locatie wordt geladen... even geduld!");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        webview.loadUrl("javascript:window.HTMLViewer.getHTML(document.getElementById('placeid').innerHTML);");
        webview.loadUrl("http://school.ceesjannolen.nl/app/index.html");
        webview.loadUrl("javascript:window.HTMLViewer.GetHTML(document.getElementById('placeadres').innerHTML);");

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                placeId = myjavascriptinterface.getPlaceID();
                Log.d("plaatsid", placeId);

                placeAdress = myjavascriptinterface.getPlaceAdress();
                Log.d("adres", placeAdress);

                Getplaceinfo(placeId);
                pDialog.dismiss();
            }
        }, 20000);

    }

    @Override
    public void onConnected(Bundle bundle) {
        //mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i("Log Scues Apicleint", "Google Places API connected.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e("error apicleint", "Google Places API connection suspended.");
    }

    public void Getplaceinfo(String placeId) {
        Log.d("place id = : ", placeId);
        if (mGoogleApiClient.isConnected() && mGoogleApiClient != null) {
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        } else {
            Toast.makeText(getApplicationContext(), " googleapicleint error", Toast.LENGTH_SHORT).show();
        }
    }

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e("errormesgae", "Place query did not complete. Error: " +
                        places.getStatus().toString());
                places.release();
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            Log.d("PLACE FOUND", place.getName() + " latlng= " + place.getLatLng());
            newlatlng = place.getLatLng();
            Log.d(" latlng set", newlatlng.toString());
            Log.d("plaats adres:",placeAdress);
            places.release();
            Toast.makeText(getApplicationContext(), newlatlng.toString(), Toast.LENGTH_SHORT).show();

            String result = newlatlng.toString() + "-//-" + placeAdress;
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result",result);
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    };


}