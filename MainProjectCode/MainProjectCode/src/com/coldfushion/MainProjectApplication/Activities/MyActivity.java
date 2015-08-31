package com.coldfushion.MainProjectApplication.Activities;

import android.app.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.coldfushion.MainProjectApplication.Helpers.JSONParser;
import com.coldfushion.MainProjectApplication.Helpers.Network;
import com.coldfushion.MainProjectApplication.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MyActivity extends Activity implements OnMapReadyCallback{
    Network network;

    //start of drawer code
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private String[] mMenuItems;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    //end of drawer code

    LatLng StartLatLng;


    private LocationManager mLocationManager;
    private Location MyLoc;
    ArrayList<Marker> markers = new ArrayList<>();

    GoogleMap Theonemap;

    //JSON STUFF
    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    List<HashMap<String, String>> uitjesList = new ArrayList<HashMap<String, String>>();
    List<HashMap<String, String>> SugUitjesList = new ArrayList<>();

    // url waar het PHPscript dat we willen zich bevind
    private static String url_all = "http://coldfusiondata.site90.net/db_get_all.php";
    private static String url_all_suggested = "http://coldfusiondata.site90.net/db_get_all_suggestion.php";

    // Progress Dialog
    private ProgressDialog pDialog;

    // We maken hier vars aan voor de JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ID ="uitjesID";
    private static final String TAG_UITJES = "Uitjes";
    private static final String TAG_NAME = "Naam";
    private static final String TAG_COORDINAAT = "Coordinaat";

    // Hier maken we de uitjes JSONArray
    JSONArray uitjes = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        network = new Network(getApplicationContext());

        //set the map
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        loadUI();

        if (network.isOnline()) {
            if (markers.size() < 1 ) {
                new LoadCoordinate().execute();
            }
        }
        else {
            Toast t = new Toast(getApplicationContext());
            t.makeText(getApplicationContext(), "Geen internet verbinding beschikbaar", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        selectItem(0);
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

    @Override
    public void onMapReady(GoogleMap map)
    {
        //set the map also in theonemap so that we can edit it later on.
        Theonemap = map;
        //get the location of the device to startlocation
        Location StartLocation = getLocation();

        //check if the startlocation is filled
        if (StartLocation != null) {
            //set the map to location of the device
            StartLatLng = new LatLng(StartLocation.getLatitude(), StartLocation.getLongitude());
            setMap(map, StartLatLng, "Huidige Locatie", "Hier bevindt u zich momenteel");
        }
        else
        {
            //need to set location to standard position if the location of the device is not known
            LatLng Rotterdam = new LatLng(51.92, 4.48);
            setMap(map, Rotterdam, "Start Locatie", "Dit is uw startlocatie");
        }

        Theonemap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener()
        {
            @Override
            public void onInfoWindowClick(Marker marker)
            {
                String markerTitle = marker.getTitle();
                int indexOfEndID = markerTitle.indexOf(", ");
                int indexOfEndIDSug = markerTitle.indexOf(". ");
                if (indexOfEndID != -1) {

                    Intent intent = new Intent(MyActivity.this, DetailUitje.class);
                    intent.putExtra("number", markerTitle.substring(0, indexOfEndID));
                    startActivity(intent);
                }
                else if ( indexOfEndIDSug != -1){
                    Intent intent = new Intent(MyActivity.this, RateUitjeItem.class);
                    intent.putExtra("number", markerTitle.substring(0, indexOfEndIDSug));
                    startActivity(intent);
                }

            }
        });

        //ADD CODE FOR ADDING MARKERS TO THE MAP FOR EVERY ACTIVITY
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                String result = data.getStringExtra("result");
                String x = result.substring(10, result.length()-1);
                double latitude = Double.parseDouble(x.substring(0, x.indexOf(",")));
                double longitude = Double.parseDouble(x.substring(x.indexOf(",") + 1));

                LatLng NewLocation = new LatLng(latitude, longitude);
                setMap(Theonemap, NewLocation, "Location", "Uw huidige locatie");

            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Geen nieuwe locatie gevonden", Toast.LENGTH_SHORT).show();
                //Write your code if there's no result
            }
        }
    }

    /*a
     * Uitjes-zoeken stuff
     */

    private class DrawerItemClickListener implements ListView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
    public void selectItem(int position){
        if(mMenuItems[position].toLowerCase().equals("locatie wijzigen")){
            Intent LocationChooseIntent = new Intent(getApplicationContext(), LocationChoose.class);
            startActivityForResult(LocationChooseIntent, 1);

        }
        else if (mMenuItems[position].toLowerCase().equals("suggestie maken")){
            Intent MakeSuggestionIntent = new Intent(getApplicationContext(), MakeSuggestion.class);
            startActivity(MakeSuggestionIntent);
        }
        else if(mMenuItems[position].toLowerCase().equals("uitje beoordelen")){
            Intent RateActivityIntent = new Intent(getApplicationContext(), RateActivities.class);
            startActivity(RateActivityIntent);
        }
        else if (mMenuItems[position].toLowerCase().equals("alle uitjes")) {
            Intent AlleUitjes = new Intent(getApplicationContext(), ResultActivity.class);
            startActivity(AlleUitjes);
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


    // get location of the device
    public Location getLocation() {
        try {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            //check if gps or network is on.
            boolean GPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean NetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            //define the locations for gps and network
            Location GPSLoc = null;
            Location NetworkLoc = null;

            //throw exception if none of the services is available
            if(!GPSEnabled && !NetworkEnabled){
                throw new Exception("Geen netwerk beschikbaar");
            }
            else {
                if (GPSEnabled) {
                    //get the location with gps
                    GPSLoc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (NetworkEnabled){
                    //get the location with network
                    NetworkLoc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }

                //check if the locations are both not null
                if (GPSLoc != null && NetworkLoc != null){
                    //check for the location with the highest accuracy and give it as result
                    if(GPSLoc.getAccuracy() >= NetworkLoc.getAccuracy()){
                        MyLoc = GPSLoc;
                    }
                    else {
                        MyLoc = NetworkLoc;
                    }
                }
                else {
                    //return the location of gps or network depending on who isnt null
                    if (GPSLoc != null){
                        MyLoc = GPSLoc;
                    }
                    else if (NetworkLoc != null){
                        MyLoc = NetworkLoc;
                    }
                }
            }
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG);
            e.printStackTrace();
        }

        //return location
        return MyLoc;
    }


    public void ShowResults(View view){
        Intent results = new Intent(getApplicationContext(), Results.class);
        double latitude = StartLatLng.latitude;
        double longitude = StartLatLng.longitude;

        results.putExtra("latitude", latitude);
        results.putExtra("longitude", longitude);
        startActivity(results);
    }

    private void setMap(GoogleMap map, LatLng latLng, String markerTitle, String markerSnippet){
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));

        map.addMarker(new MarkerOptions()
                .title(markerTitle)
                .snippet(markerSnippet)
                .position(latLng));
    }

    private void loadUI(){
        //for the menu drawer
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
                selectItem(0);
            }

            public void onDrawerOpened(View view) {
                mDrawerList.bringToFront();
                mDrawerLayout.requestLayout();
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }

        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        //set the standard selected item on  0 --> the first item (kaart)
        selectItem(0);
        //end of menu drawer
    }

    //JSON CLASS FOR COORDINATES AND NAME
    class LoadCoordinate extends AsyncTask<String, String, String> {

        /**
         * Voordat we de taak starten laten we netjes een "zandloper" zien
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MyActivity.this);
            pDialog.setMessage("Uitjes worden opgehaald... even geduld!");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();


        }

        /**
         * getting All from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL

            JSONObject json = null;
            try {
                json = jParser.makeHttpRequest(url_all, "GET", params);
            }catch (Exception e){
                e.printStackTrace();
            }
            if (json == null) {
                Log.d("jsonechek", "jsonempty");
            }
            else {
                // Check your log cat for JSON reponse
                Log.d("Uitjes: ", json.toString());
            }
            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    uitjes = json.getJSONArray(TAG_UITJES);

                    // looping through All Products
                    for (int i = 0; i < uitjes.length(); i++) {
                        JSONObject c = uitjes.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_ID);
                        String name = c.getString(TAG_NAME);
                        String coordinaat = c.getString(TAG_COORDINAAT);

                        //Log.d("coordinaat van "+ name, coordinaat + " <--");


                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_ID, id);
                        map.put(TAG_NAME, name);
                        map.put(TAG_COORDINAAT, coordinaat);

                        map.values();
                        if(Theonemap.equals(null) != true)
                        {

                        }

                        // adding HashList to ArrayList
                        uitjesList.add(map);

                    }

                }
                else
                {
                    // no products found
                    Log.d("Uitjes status", "Geen uitjes");
                }

            }
            catch
                    (JSONException e)
            {
                e.printStackTrace();
            }

            //CODE FOR THE SUGGESTED UITJES
            JSONObject json1 = null;
            try {
                json1 = jParser.makeHttpRequest(url_all_suggested, "GET", params);
            }catch (Exception e){
                e.printStackTrace();
            }
            if (json1 == null) {
                Log.d("jsonechek", "jsonempty");
            }
            else {
                // Check your log cat for JSON reponse
                Log.d("Suggested Uitjes: ", json1.toString());
            }
            try {
                // Checking for SUCCESS TAG
                int success = json1.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    uitjes = json1.getJSONArray(TAG_UITJES);

                    // looping through All Products
                    for (int i = 0; i < uitjes.length(); i++) {
                        JSONObject c = uitjes.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_ID);
                        String name = c.getString(TAG_NAME);
                        String coordinaat = c.getString(TAG_COORDINAAT);

                        //Log.d("coordinaat van "+ name, coordinaat + " <--");


                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_ID, id);
                        map.put(TAG_NAME, name);
                        map.put(TAG_COORDINAAT, coordinaat);

                        map.values();

                        // adding HashList to ArrayList
                        SugUitjesList.add(map);

                    }

                }
                else
                {
                    // no products found
                    Log.d("Uitjes status", "Geen uitjes");
                }

            }
            catch
                    (JSONException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            setMarkers();
            pDialog.dismiss();
            // updating UI from Background Thread

        }

    }

    private void setMarkers(){
        for (int i = 0; i < uitjesList.size(); i++){
            String ids = uitjesList.get(i).get(TAG_ID);
            String Coordinaten = uitjesList.get(i).get(TAG_COORDINAAT);
            String Naam = ids + ", " + uitjesList.get(i).get(TAG_NAME);
            Log.d(" naam", Naam);
            int commaLocation = Coordinaten.indexOf(",");
            String Coordinaat_Lat = Coordinaten.substring(0, commaLocation);
            Log.d(" coordinaatlat", Coordinaat_Lat);
            String Coordinaat_Lng = Coordinaten.substring(commaLocation + 1);
            Log.d(" coordinaatlng", Coordinaat_Lng);

            double lat = Double.parseDouble(Coordinaat_Lat);
            double lng = Double.parseDouble(Coordinaat_Lng);

            LatLng latLng = new LatLng(lat, lng);

            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(Naam).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            Marker marker = Theonemap.addMarker(markerOptions);


            markers.add(marker);

        }

        for (int i = 0; i < SugUitjesList.size(); i++){
            String ids = SugUitjesList.get(i).get(TAG_ID);
            String Coordinaten = SugUitjesList.get(i).get(TAG_COORDINAAT);
            String Naam = ids + ". " + SugUitjesList.get(i).get(TAG_NAME);
            Log.d(" naam", Naam);
            int commaLocation = Coordinaten.indexOf(",");
            String Coordinaat_Lat = Coordinaten.substring(0, commaLocation);
            Log.d(" coordinaatlat", Coordinaat_Lat);
            String Coordinaat_Lng = Coordinaten.substring(commaLocation + 1);
            Log.d(" coordinaatlng", Coordinaat_Lng);

            double lat = Double.parseDouble(Coordinaat_Lat);
            double lng = Double.parseDouble(Coordinaat_Lng);

            LatLng latLng = new LatLng(lat, lng);

            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(Naam).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).alpha(0.5f);

            Marker marker = Theonemap.addMarker(markerOptions);

            markers.add(marker);

        }


    }

}


