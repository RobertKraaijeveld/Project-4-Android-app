package com.coldfushion.MainProjectApplication.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ceesjan on 22-5-2015.
 */
public class MakeSuggestion extends Activity {
    Network network;
    //start of drawer code
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private String[] mMenuItems;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    //end of drawer code

    Button submitButton;
    Button getLocationButton;

    EditText editText_naam;
    EditText editText_beschrijving;
    EditText editText_telefoon;

    Spinner spinner_weer;
    Spinner spinner_categorie;

    String Coordinaten;
    String Straat;
    String Stad;
    String Postcode;

    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.makesuggestion_layout);

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
                selectItem(2);
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

        //end code for the drawer'
        submitButton = (Button)findViewById(R.id.button_Suggestion_submit);
        getLocationButton = (Button)findViewById(R.id.button_Suggestion_getlocation);

        editText_naam = (EditText)findViewById(R.id.EditText_Suggestion_Name);
        editText_beschrijving = (EditText)findViewById(R.id.EditText_Suggestion_Beschrijving);
        editText_telefoon = (EditText)findViewById(R.id.EditText_Suggestion_Telefoon);

        spinner_categorie = (Spinner)findViewById(R.id.spinner_Categorie);
        spinner_weer = (Spinner)findViewById(R.id.spinner_weertype);

        String[] weeritems = new String[]{"Sunny", "Cloudy", "Rainy"};
        ArrayAdapter<String> weer_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, weeritems);
        spinner_weer.setAdapter(weer_adapter);

        String[] categorieen = new String[]{"Pretpark", "Restaurant", "Museum"};
        ArrayAdapter<String> categorie_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categorieen);
        spinner_categorie.setAdapter(categorie_adapter);
    }

    //Deze methode wordt gecalled als de submitbutton geklikt is.
    public void AddToDatabase(View view)
    {
        //add location added check
        if(editText_naam.getText().equals("") ||  editText_beschrijving.getText().equals("") || spinner_weer.getSelectedItem().toString().equals("") || spinner_categorie.getSelectedItem().toString().equals("") )
        {
            Toast.makeText(getApplicationContext(), "U hebt een van de velden niet ingevoerd!", Toast.LENGTH_SHORT);
        }
        else if ( Stad == null || Postcode == null || Straat == null || Coordinaten == null) {
            Toast.makeText(getApplicationContext(), "U heeft geen locatie geselecteerd" , Toast.LENGTH_SHORT);
        }
        else
        {


            String newNaam = editText_naam.getText().toString().replace(" ", "+");
            String newBeschrijving = editText_beschrijving.getText().toString().replace(" ", "+");
            String newCategorie = spinner_categorie.getSelectedItem().toString().replace(" ", "+");
            String newWeerType = spinner_weer.getSelectedItem().toString().replace(" ", "+");
            String newtelefoon = editText_telefoon.getText().toString().replace(" ", "+");
            String newStraat = Straat.replace(" ", "+");
            String newPostcode = Postcode.replace(" ", "+");
            String newStad = Stad.replace(" ", "+");
            String parameters_url =

                    "NaamVar=" + newNaam + "&WeerTypeVar=" + newWeerType + "&BeschrijvingVar=" + newBeschrijving +
                            "&CategorieVar=" + newCategorie + "&TelefoonVar=" + newtelefoon +
                            "&StraatVar=" + newStraat +  "&PostCodeVar=" + newPostcode +
                            "&StadVar=" + newStad + "&CoordinaatVar=" + Coordinaten ;

            final String insert_url = "http://coldfusiondata.site90.net/db_insert_suggestion.php?" + parameters_url + "";
            Log.d("String url", insert_url);
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            JSONParser jsonParser = new JSONParser();
            jsonParser.simpleGetJSONfromURL(insert_url);
        }
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if(resultCode == RESULT_OK){
                String result = data.getStringExtra("result");
                int endCoor = result.indexOf("-//-");
                Coordinaten = result.substring(10, endCoor -1);
                Log.d("coordinate", Coordinaten  + " << dit zijn de coordinaten");
                Log.d("rest", result.substring(endCoor + 4));
                String rest = result.substring(endCoor +4);
                int x = rest.indexOf(",")+2;
                Log.d("straat", rest.substring(0, x-2));
                Log.d("postcode", rest.substring(x, x + 8));
                Log.d("Plaats", rest.substring(x+8, rest.lastIndexOf(",")));


                Straat = rest.substring(0, x-2);
                Postcode = rest.substring(x, x + 8);
                Stad = rest.substring(x+8, rest.lastIndexOf(","));

            }
            if (resultCode == RESULT_CANCELED)
            {
                Toast.makeText(getApplicationContext(), "Geen nieuwe locatie gevonden", Toast.LENGTH_SHORT).show();
                //Write your code if there's no result
            }
        }
    }







    @Override
    protected void onStart() {
        super.onStart();
        selectItem(2);
    }

    //start of drawer code
    @Override
    protected void onResume() {
        super.onResume();
        selectItem(2);
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
        if(mMenuItems[position].toLowerCase().equals("locatie wijzigen")){
            this.finish();
            Intent LocationChooseIntent = new Intent(getApplicationContext(), LocationChoose.class);
            startActivity(LocationChooseIntent);
        }
        else if (mMenuItems[position].toLowerCase().equals("bekijk uitjes op kaart")){
            this.finish();
        }
        else if (mMenuItems[position].toLowerCase().equals("alle uitjes")) {
            this.finish();
            Intent AlleUitjes = new Intent(getApplicationContext(), ResultActivity.class);
            startActivity(AlleUitjes);
        }

        else if (mMenuItems[position].toLowerCase().equals("suggestie maken")){

        }
        else if(mMenuItems[position].toLowerCase().equals("uitje beoordelen")){
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
    public void Suggestion_GetLocation(View view){
        if (network.isOnline()) {
            Toast.makeText(getApplicationContext(), "Kies een locatie", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, SimpleLocationChoose.class);
            startActivityForResult(i, 2);
        }
        else {
            Toast t = new Toast(getApplicationContext());
            t.makeText(getApplicationContext(), "Geen internet verbinding beschikbaar", Toast.LENGTH_LONG).show();
        }
    }


}