package com.coldfushion.MainProjectApplication.Activities;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.view.View;
import android.widget.*;
import com.coldfushion.MainProjectApplication.Helpers.JSONParser;
import com.coldfushion.MainProjectApplication.Helpers.Network;
import com.coldfushion.MainProjectApplication.Helpers.getCurrentWeather;
import com.coldfushion.MainProjectApplication.R;
import com.google.android.gms.maps.model.LatLng;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;


/**
 * Created by Kraaijeveld on 21-5-2015.
 * Deze Activity geeft het resultaat van onze PHP-requests weer.
 * Voor nu, krijgen we alleen alle namen van alle uitjes in onze DB terug.
 */

public class Results extends ListActivity {
    Network network;

    double latitude_Home;
    double longitude_Home;

    String[] mTypes;

    int Type = 0;
    // 0 = geen | 1 = Pretpark | 2 = Restaurant | 3 = museum

    getCurrentWeather currentweather;

    public Toast t;
    ListView listViewlist;
    TextView textView;
    Button button;

    Spinner spinnerType ;

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    List<HashMap<String, String>> uitjesList;

    List<HashMap<String,String>> Pretpark = new ArrayList<>();
    List<HashMap<String,String>> Museum = new ArrayList<>();
    List<HashMap<String,String>> Restaurant = new ArrayList<>();

    // url waar het PHPscript dat we willen zich bevind
    private static String url_all_products = "http://coldfusiondata.site90.net/db_get_all.php";

    // We maken hier vars aan voor de JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_UITJES = "Uitjes";
    private static final String TAG_PID = "uitjesID";
    private static final String TAG_NAME = "Naam";
    private static final String TAG_TYPE = "Categorie";
    private static final String TAG_AFSTAND = "afstand";
    private static final String TAG_COORDINATEN = "Coordinaat";

    // Hier maken we de uitjes JSONArray
    JSONArray uitjes = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {


        Intent receiveIntent = this.getIntent();
        latitude_Home = receiveIntent.getDoubleExtra("latitude", 51.92);
        longitude_Home = receiveIntent.getDoubleExtra("longitude", 4.48);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.results_layout);

        network = new Network(getApplicationContext());

        textView = (TextView)findViewById(R.id.textview_noUitjes);
        button = (Button)findViewById(R.id.ButtonIDGoback);

        mTypes = getResources().getStringArray(R.array.Types);

        spinnerType = (Spinner)findViewById(R.id.spinner_SelectType);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemTypeChange(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //getlistview
        listViewlist = getListView();
        listViewlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                Intent DetailPageIntent = new Intent(getApplicationContext(), DetailUitje.class);
                if (Type == 0) {
                    DetailPageIntent.putExtra("number", uitjesList.get(position).get("uitjesID"));
                }
                else if ( Type == 1) {
                    DetailPageIntent.putExtra("number", Pretpark.get(position).get("uitjesID"));
                }
                else if (Type == 2){
                    DetailPageIntent.putExtra("number", Restaurant.get(position).get("uitjesID"));
                }
                else {
                    DetailPageIntent.putExtra("number", Museum.get(position).get("uitjesID"));
                }
                startActivity(DetailPageIntent);

            }
        });

        //Bij het starten van deze activity maken we een KVP hashmap om de uitjes in te bewaren,
        //En starten we de LoadAllUitjes thread.
        uitjesList = new ArrayList<HashMap<String, String>>();
        if (network.isOnline()) {
            new LoadAllUitjes().execute();
        }
        else {
            Toast t = new Toast(getApplicationContext());
            t.makeText(getApplicationContext(), "Geen internet verbinding beschikbaar", Toast.LENGTH_SHORT).show();
        }

        distancecheck();

    }



    /**
     * Deze achtergrondthread doet het daadwerkelijke werk:
     * Het callen van ons PHP script en het binnenhalen van de JSON
     * Response dat het PHP script ons teruggeeft.
     * */
    public class LoadAllUitjes extends AsyncTask<String, String, String> {

        /**
         * Voordat we de taak starten laten we netjes een "zandloper" zien
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Results.this);
            pDialog.setMessage("Uitjes worden opgehaald... even geduld!");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

            currentweather = new getCurrentWeather(getApplicationContext());
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
                json = jParser.makeHttpRequest(url_all_products, "GET", params);
            }catch (Exception e){
                e.printStackTrace();
            }
            if (json == null) {
                Log.d("jsonechek", "jsonempty");
            }
            // Check your log cat for JSON reponse
            Log.d("Uitjes: ", json.toString());

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
                        String id = c.getString(TAG_PID);
                        String name = c.getString(TAG_NAME);
                        String type = c.getString(TAG_TYPE);
                        String coordinaten = c.getString(TAG_COORDINATEN);


                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_PID, id);
                        map.put(TAG_NAME, name);
                        map.put(TAG_TYPE, type);
                        map.put(TAG_COORDINATEN, coordinaten);

                        map.values();

                        // adding HashList to ArrayList
                        uitjesList.add(map);

                    }

                } else {
                    // no products found
                    Log.d("Uitjes status", "Geen uitjes");
                    //set the textview to visible
                    textView.getHandler().post(new Runnable() {
                        public void run() {
                            textView.setVisibility(View.VISIBLE);
                        }
                    });

                    textView.setText("Geen uitjes gevonden");
                    //set the button to visible
                    button.getHandler().post(new Runnable() {
                        public void run() {
                            button.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {

            FilterOnType(uitjesList);

            //call function for filtering
            ListAdapter adapter = new SimpleAdapter(
                    Results.this, uitjesList,
                    R.layout.resultlistitem, new String[]{TAG_PID,
                    TAG_NAME, TAG_AFSTAND},
                    new int[]{R.id.uitjesID, R.id.Naam,R.id.Afstand});

            // updating listview
            setListAdapter(adapter);


            // dismiss the dialog after getting all products
            pDialog.dismiss();
        }

    }
    public void GoBackButton_Resultlist(View v){
        this.finish();
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void FilterOnType(List<HashMap<String, String>> uitjeslijst){

        List<HashMap<String, String>> newlist = new ArrayList<>();
        float [] dists = new float[1];

        //sorting on distance
        for (int i = 0; i < uitjeslijst.size(); i ++) {

            //getting coordinaten
            String coordinaten = uitjeslijst.get(i).get(TAG_COORDINATEN);
            int commaLocation = coordinaten.indexOf(",");
            String Coordinaat_Lat = coordinaten.substring(0, commaLocation);
            String Coordinaat_Lng = coordinaten.substring(commaLocation + 1);
            //converting to doubles (lat long)
            double lat = Double.parseDouble(Coordinaat_Lat);
            double lng = Double.parseDouble(Coordinaat_Lng);

            //getting distance between home and selected thing
            Location.distanceBetween(latitude_Home, longitude_Home, lat, lng,  dists);
            Log.d("distance (Home - Uitje)", dists[0] * 0.000621371192f + "");
            //adding the distance to the hashmap
            uitjeslijst.get(i).put(TAG_AFSTAND, dists[0]*0.000621371192f + "");

        }

        //add all to a extra list and clear the total list
        for (int i = 0 ; i < uitjeslijst.size(); i ++){
            newlist.add(uitjeslijst.get(i));
        }
        uitjeslijst.clear();


        //loop through the extra list
        for (int i = 0 ; i < newlist.size(); i ++) {

            double newlistaftand = Double.parseDouble(newlist.get(i).get(TAG_AFSTAND)) ;
            Log.d( "adts", newlistaftand + "");
            if (uitjeslijst.size() < 1){
                uitjeslijst.add(newlist.get(i));

            }
            else {
                //inside loop the total list.
                int lenghtlist = uitjeslijst.size();
                for (int j = 0; j < uitjeslijst.size(); j++) {
                    double uitjeslijstaftand = Double.parseDouble(uitjeslijst.get(j).get(TAG_AFSTAND));
                    //check if the distance is smaller then the current item of total list.
                    if (newlistaftand < uitjeslijstaftand) {
                        uitjeslijst.add(j, newlist.get(i));
                        break;

                    }
                    if (j == lenghtlist - 1) {
                        uitjeslijst.add(newlist.get(i));
                    }

                }
            }
        }

        // at the end clear extra list.
        newlist.clear();


        //filtering

        //loop through the list checking for the types and adding to the right lists
        for (int i = 0 ; i < uitjeslijst.size(); i++){
            String Type = uitjeslijst.get(i).get(TAG_TYPE);
            if (Type.equals("Museum")){
                Museum.add(uitjeslijst.get(i));
            }else if(Type.equals("Pretpark")){
                Pretpark.add(uitjeslijst.get(i));
            }else {
                Restaurant.add(uitjeslijst.get(i));
            }
        }


    }

    private void itemTypeChange(int number){

        listViewlist.setAdapter(null);
        if(mTypes[number].equals("Geen")){
            Type = 0;
            ListAdapter adapter = new SimpleAdapter(
                    Results.this, uitjesList,
                    R.layout.resultlistitem, new String[]{TAG_PID,
                    TAG_NAME, TAG_AFSTAND},
                    new int[]{R.id.uitjesID, R.id.Naam,R.id.Afstand});

            // updating listview
            setListAdapter(adapter);

        }
        else if(mTypes[number].equals("Pretpark")){
            Type = 1;
            ListAdapter adapter = new SimpleAdapter(
                    Results.this, Pretpark,
                    R.layout.resultlistitem, new String[]{TAG_PID,
                    TAG_NAME, TAG_AFSTAND},
                    new int[]{R.id.uitjesID, R.id.Naam,R.id.Afstand});

            // updating listview
            setListAdapter(adapter);

        }
        else if(mTypes[number].equals("Restaurant")){
            Type = 2;
            ListAdapter adapter = new SimpleAdapter(
                    Results.this, Restaurant,
                    R.layout.resultlistitem, new String[]{TAG_PID,
                    TAG_NAME, TAG_AFSTAND},
                    new int[]{R.id.uitjesID, R.id.Naam,R.id.Afstand});

            // updating listview
            setListAdapter(adapter);

        }
        else {
            Type = 3;
            ListAdapter adapter = new SimpleAdapter(
                    Results.this, Museum,
                    R.layout.resultlistitem, new String[]{TAG_PID,
                    TAG_NAME, TAG_AFSTAND},
                    new int[]{R.id.uitjesID, R.id.Naam,R.id.Afstand});

            // updating listview
            setListAdapter(adapter);

        }
    }

    public void distancecheck(){
        Location Home  = new Location("Home");
        Home.setLatitude(latitude_Home);
        Home.setLongitude(longitude_Home);

        float [] distss = new float[1];
        Location.distanceBetween(latitude_Home, longitude_Home, 51.92, 4.48,  distss);
        Log.d(" dist 1122 ==== ", distss[0]*0.000621371192f + "");
    }
}