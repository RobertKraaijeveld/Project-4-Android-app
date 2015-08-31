package com.coldfushion.MainProjectApplication.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
 * Created by ceesjan on 22-5-2015.
 */
public class LocationChoose extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    Network network;
    //start of drawer code
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private String[] mMenuItems;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    //end of drawer code


    Boolean latlngset = false;
    WebView webview;
    MyJavaScriptInterface myjavascriptinterface = new MyJavaScriptInterface(this);
    GoogleApiClient mGoogleApiClient;
    private LatLng newlatlng;
    String placeId;
    ProgressDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locationchoose_layout);

        network = new Network(getApplicationContext());

        //code for the drawer
        mTitle = mDrawerTitle = getTitle();
        mMenuItems = getResources().getStringArray(R.array.menu_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        //set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mMenuItems));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
                selectItem(1);
            }

            public void onDrawerOpened(View view) {
                mDrawerList.bringToFront();
                mDrawerLayout.requestLayout();
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }

        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        //end code for the drawer

        if (network.isOnline()) {
            webview = (WebView) findViewById(R.id.WebviewLocationChoose);
            webview.getSettings().setJavaScriptEnabled(true);

            webview.addJavascriptInterface(myjavascriptinterface, "HTMLViewer");

            webview.loadUrl("http://school.ceesjannolen.nl/app/index.html");

            //googleapiclient
            mGoogleApiClient = new GoogleApiClient.Builder(LocationChoose.this).addApi(Places.GEO_DATA_API).addConnectionCallbacks(this).build();
            mGoogleApiClient.connect();
        }
        else {
            Toast t = new Toast(getApplicationContext());
            t.makeText(getApplicationContext(), "Geen internet verbinding beschikbaar", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    //start of drawer code
    @Override
    protected void onStart() {
        super.onStart();
        selectItem(1);
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        selectItem(1);
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    public void selectItem(int position) {
        if (mMenuItems[position].toLowerCase().equals("bekijk uitjes op kaart")) {
            this.finish();
        } else if (mMenuItems[position].toLowerCase().equals("alle uitjes")) {
            this.finish();
            Intent AlleUitjes = new Intent(getApplicationContext(), ResultActivity.class);
            startActivity(AlleUitjes);
        } else if (mMenuItems[position].toLowerCase().equals("suggestie maken")) {
            this.finish();
            Intent MakeSuggestionIntent = new Intent(getApplicationContext(), MakeSuggestion.class);
            startActivity(MakeSuggestionIntent);
        } else if (mMenuItems[position].toLowerCase().equals("uitje beoordelen")) {
            this.finish();
            Intent RateActivityIntent = new Intent(getApplicationContext(), RateActivities.class);
            startActivity(RateActivityIntent);
        }
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mMenuItems[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    //end of drawer code

    //location choose event click
    public void ChooseLocation(View view) {

        pDialog = new ProgressDialog(LocationChoose.this);
        pDialog.setMessage("Locatie wordt geladen... even geduld!");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();


            webview.loadUrl("javascript:window.HTMLViewer.getHTML(document.getElementById('placeid').innerHTML);");


            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    placeId = myjavascriptinterface.getPlaceID();

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
                    places.release();
                    Toast.makeText(getApplicationContext(), newlatlng.toString(), Toast.LENGTH_SHORT).show();

                    String result = newlatlng.toString();
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result", result);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            };


        }