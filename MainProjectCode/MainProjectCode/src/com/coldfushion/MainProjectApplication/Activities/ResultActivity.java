package com.coldfushion.MainProjectApplication.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.coldfushion.MainProjectApplication.Helpers.JSONParser;
import com.coldfushion.MainProjectApplication.Helpers.Network;
import com.coldfushion.MainProjectApplication.Helpers.getCurrentWeather;
import com.coldfushion.MainProjectApplication.R;
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

public class ResultActivity extends ListActivity {
    Network network;

    getCurrentWeather currentweather;
    //start of drawer code
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private String[] mMenuItems;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    //end of drawer code

    public Toast t;
    ListView listViewlist;
    TextView textView;
    Button button;

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    List<HashMap<String, String>> uitjesList;

    // url waar het PHPscript dat we willen zich bevind
    private static String url_all_products = "http://coldfusiondata.site90.net/db_get_all.php";

    // We maken hier vars aan voor de JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_UITJES = "Uitjes";
    private static final String TAG_PID = "uitjesID";
    private static final String TAG_NAME = "Naam";
    private static final String TAG_WEERTYPE = "weerType";

    // Hier maken we de uitjes JSONArray
    JSONArray uitjes = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resultlist);

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
                selectItem(4);
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

        //end code for the drawer


        textView = (TextView)findViewById(R.id.textview_noUitjes);
        button = (Button)findViewById(R.id.ButtonIDGoback);

        //getlistview
        listViewlist = getListView();
        listViewlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                Toast.makeText(ResultActivity.this, "You Clicked at " + uitjesList.get(position).get("uitjesID"), Toast.LENGTH_SHORT).show();
                Intent DetailPageIntent = new Intent(getApplicationContext(), DetailUitje.class);
                DetailPageIntent.putExtra("number", uitjesList.get(position).get("uitjesID"));
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

    }

    @Override
    protected void onStart() {
        super.onStart();
        selectItem(4);
    }

    //start of drawer code
    @Override
    protected void onResume() {
        super.onResume();
        selectItem(4);
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
    private class DrawerItemClickListener implements ListView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
    public void selectItem(int position){
        if (mMenuItems[position].toLowerCase().equals("bekijk uitjes op kaart")){
            finish();
        }
        else if(mMenuItems[position].toLowerCase().equals("locatie wijzigen")){
            this.finish();
            Toast.makeText(getApplicationContext(), "lcaotie wijzigen", Toast.LENGTH_SHORT).show();
            Intent LocationChooseIntent = new Intent(getApplicationContext(), LocationChoose.class);
            startActivity(LocationChooseIntent);
        }
        else if (mMenuItems[position].toLowerCase().equals("suggestie maken")){
            this.finish();
            Toast.makeText(getApplicationContext(), "suggestie maken ofzo", Toast.LENGTH_SHORT).show();
            Intent MakeSuggestionIntent = new Intent(getApplicationContext(), MakeSuggestion.class);
            startActivity(MakeSuggestionIntent);
        }
        else if(mMenuItems[position].toLowerCase().equals("uitje beoordelen")){
            this.finish();
            Toast.makeText(getApplicationContext(), "uitjes beoordelen", Toast.LENGTH_SHORT).show();
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
            pDialog = new ProgressDialog(ResultActivity.this);
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
                            String weertype = c.getString(TAG_WEERTYPE);

                            Log.d("weertype van "+ name, weertype + " <--");


                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            map.put(TAG_PID, id);
                            map.put(TAG_NAME, name);
                            map.put(TAG_WEERTYPE, weertype);

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
            //call function for filtering
            uitjesList = FilterOnType(uitjesList);

            ListAdapter adapter = new SimpleAdapter(
                    ResultActivity.this, uitjesList,
                    R.layout.resultlistitem, new String[]{TAG_PID,
                    TAG_NAME},
                    new int[]{R.id.uitjesID, R.id.Naam});

            // updating listview
            setListAdapter(adapter);


            // dismiss the dialog after getting all products
            pDialog.dismiss();
        }

    }
    public void GoBackButton_Resultlist(View v){
        this.finish();
    }



    private List<HashMap<String, String>> FilterOnType(List<HashMap<String, String>> uitjeslijst){
        //defines lists for THE 3 types of weather
        List<HashMap<String,String>> Sunny = new ArrayList<>();
        List<HashMap<String,String>> Cloudy = new ArrayList<>();
        List<HashMap<String,String>> Rainy = new ArrayList<>();

        //loop through the list checking for the types and adding to the right lists
        for (int i = 0 ; i < uitjeslijst.size(); i++){
            String Weertype = uitjeslijst.get(i).get(TAG_WEERTYPE);
            Log.d("weertype ", Weertype);
            if (Weertype.equals("Sunny")){
                Sunny.add(uitjeslijst.get(i));
            }else if(Weertype.equals("Rainy")){
                Rainy.add(uitjeslijst.get(i));
            }else {
                Cloudy.add(uitjeslijst.get(i));
            }
        }
        //gettting the weather of the moment
        String filteredweather = currentweather.filterWeatherText();
        //clear the return list
        uitjeslijst.clear();
        //filter for sunny
        if (filteredweather.equals("Sunny")){
            for (int i = 0; i < Sunny.size(); i ++){
                uitjeslijst.add(Sunny.get(i));
            }for (int i = 0; i < Cloudy.size(); i ++){
                uitjeslijst.add(Cloudy.get(i));
            }for (int i = 0; i < Rainy.size(); i ++){
                uitjeslijst.add(Rainy.get(i));
            }
        }
        //filter for rainy
        else if(filteredweather.equals("Rainy")){
            for (int i = 0; i < Rainy.size(); i ++){
                uitjeslijst.add(Rainy.get(i));
            }for (int i = 0; i < Cloudy.size(); i ++){
                uitjeslijst.add(Cloudy.get(i));
            }for (int i = 0; i < Sunny.size(); i ++){
                uitjeslijst.add(Sunny.get(i));
            }
        }
        //filter  for cloudy
        else {for (int i = 0; i < Cloudy.size(); i ++){
                uitjeslijst.add(Cloudy.get(i));
            }for (int i = 0; i < Sunny.size(); i ++){
                uitjeslijst.add(Sunny.get(i));
            }for (int i = 0; i < Rainy.size(); i ++){
                uitjeslijst.add(Rainy.get(i));
            }
        }
        return uitjeslijst;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}