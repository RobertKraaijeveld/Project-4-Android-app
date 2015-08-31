package com.coldfushion.MainProjectApplication.Helpers;

/**
 * Created by Robert on 9-6-2015.
 */

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Deze achtergrondthread doet het daadwerkelijke werk:
 * Het callen van ons PHP script en het binnenhalen van de JSON
 * Response dat het PHP script ons teruggeeft.
 * */


public class getGooglePlacesData extends AsyncTask<String, String, String> {

    // Progress Dialog
    private ProgressDialog pDialog;

    public String google_places_url = "";
    public String id = "";
    public boolean suggestion = true;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    List<HashMap<String, String>> uitjesList = new ArrayList<HashMap<String, String>>();

    // url waar het PHPscript dat we willen zich bevind
    //ID MOET NOG HIERIN
    private  String url_all_suggestions = "http://coldfusiondata.site90.net/db_get_details_suggestion.php?id=";


    // We maken hier vars aan voor de JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_UITJES = "Uitjes";
    private static final String TAG_PLACEID = "PlaceID";

    // Hier maken we de uitjes JSONArray
    JSONArray uitjes = null;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * getting All from url
     */
    protected String doInBackground(String... args) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        // getting JSON string from URL

        JSONObject json = null;
        try {
            if (suggestion) {
                json = jParser.makeHttpRequest(url_all_suggestions + id, "GET", params);
            }
            else{

                url_all_suggestions = "http://coldfusiondata.site90.net/db_get_details.php?id=";
                Log.d("url", url_all_suggestions + id);
                json = jParser.makeHttpRequest(url_all_suggestions + id, "GET", params);
            }
        } catch (Exception e) {
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
                Log.d("uitjes", uitjes.toString());
                // looping through All Products
                for (int i = 0; i < uitjes.length(); i++)
                {
                    JSONObject c = uitjes.getJSONObject(i);
                    Log.d("c", c.toString());
                    // Storing each json item in variable
                    String placeid = c.getString(TAG_PLACEID);
                    Log.d("placeid", placeid);
                    // creating new HashMap
                    HashMap<String, String> map = new HashMap<String, String>();

                    // adding each child node to HashMap key => value
                    map.put(TAG_PLACEID, placeid);

                    map.values();

                    // adding HashList to ArrayList
                    uitjesList.add(map);
                }

                /*
                * OPENINGSTIJDENREQUEST
                */

                //Hier maken we de url voor de volgende request, die naar de Google places api voor de openingstijden
                google_places_url = "http://coldfusiondata.site90.net/google_get_opening_hours.php?placeid=" +
                        uitjesList.get(0).get(TAG_PLACEID);

                Log.d("url google places", google_places_url);


                return null;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }



}




