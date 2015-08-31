package com.coldfushion.MainProjectApplication.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.coldfushion.MainProjectApplication.Helpers.JSONParser;
import com.coldfushion.MainProjectApplication.Helpers.Network;
import com.coldfushion.MainProjectApplication.Helpers.getGooglePlacesData;
import com.coldfushion.MainProjectApplication.Helpers.getWebpageContent;
import com.coldfushion.MainProjectApplication.R;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ceesjan on 29-5-2015.
 */
public class RateUitjeItem extends Activity {
    Network network;

    public String openingstijden = "";
    TextView textViewBeschrijving;
    TextView textViewCategorie;
    TextView textViewWeertype;
    TextView textViewTelefoon;
    TextView textViewStraat;
    TextView textViewPostcode;
    TextView textViewStad;
    TextView textViewName;
    TextView textViewOpeninghours;
    TextView textViewPercentage;

    Button buttonVoteUp;
    Button buttonVoteDown;

    Button button_telefoonbutton;

    private int upVotes;
    private int downVotes;
    private int totalVotes;

    String naam = "";
    String categorie = "";
    String beschrijving = "";
    String stad = "";
    String straat = "";
    String postcode = "";
    String telefoon = "";
    String weertype = "";
    String coordinaat = "";

    // Progress Dialog
    private ProgressDialog pDialog;

    private String PhoneNumber;

    ArrayList<HashMap<String, String>> uitjesList;
    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    // url waar het PHPscript dat we willen zich bevind
    private String id_detail = "";
    private String url_get_details = "http://coldfusiondata.site90.net/db_get_details_suggestion.php?id=";

    // We maken hier vars aan voor de JSON Node names
    private static final String TAG_UITJES = "Uitjes";
    private static final String TAG_NAME = "Naam";
    private static final String TAG_WEERTYPE = "WeerType";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_BESCHRIJVING = "Beschrijving";
    private static final String TAG_CATEGORIE = "Categorie";
    private static final String TAG_TELEFOON = "Telefoon";
    private static final String TAG_STRAAT = "Straat";
    private static final String TAG_POSTCODE = "PostCode";
    private static final String TAG_STAD = "Stad";
    private static final String TAG_COORDINAAT = "Coordinaat";
    private static final String TAG_UPVOTECOUNT = "upVoteCount";
    private static final String TAG_DOWNVOTECOUNT = "downVoteCount";

    public boolean hasVoted = false;

    // Hier maken we de uitjes JSONArray
    JSONArray uitjes = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rateuitjeitem_layout);

        network = new Network(getApplicationContext());

        Bundle extras = getIntent().getExtras();
        id_detail = extras.get("number").toString();

        textViewStraat = (TextView) findViewById(R.id.Rate_Straat);
        textViewPostcode = (TextView) findViewById(R.id.Rate_Postcode);
        textViewStad = (TextView) findViewById(R.id.Rate_Plaats);
        textViewTelefoon = (TextView) findViewById(R.id.Rate_Telefoon);
        textViewBeschrijving = (TextView) findViewById(R.id.Rate_Beschrijving);
        textViewCategorie = (TextView) findViewById(R.id.Rate_Categorie);
        textViewName = (TextView) findViewById(R.id.Rate_Name);
        textViewWeertype = (TextView) findViewById(R.id.Rate_Weertype);
        textViewOpeninghours = (TextView)findViewById(R.id.Rate_Openingstijden);
        textViewPercentage = (TextView)findViewById(R.id.Rate_Percentage);
        buttonVoteDown = (Button)findViewById(R.id.DownVote_Button);
        buttonVoteUp = (Button)findViewById(R.id.UpVote_Button);
        button_telefoonbutton = ( Button)findViewById(R.id.telefoonbutton);

        if (network.isOnline()) {
            getGooglePlacesData getGooglePlacesData = new getGooglePlacesData();
            getGooglePlacesData.id = id_detail;
            getGooglePlacesData.execute();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String google_places_url = getGooglePlacesData.google_places_url;

                    getWebpageContent getWebpageContent = new getWebpageContent();
                    getWebpageContent.readWebpage(google_places_url);
                    Handler handler1 = new Handler();
                    handler1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            openingstijden = getWebpageContent.openingstijden;

                            Log.d("openingstijden ", openingstijden);

                            textViewOpeninghours.setText(openingstijden);
                        }
                    }, 3000);
                }
            }, 3000);
            uitjesList = new ArrayList<HashMap<String, String>>();
            new LoadDetailsSuggestedUitjes().execute();
        }
        else {
            Toast t = new Toast(getApplicationContext());
            t.makeText(getApplicationContext(), "Geen internet verbinding beschikbaar", Toast.LENGTH_LONG).show();
        }

    }

    public void ShareButton(View view)
    {
        Intent i=new Intent(android.content.Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Uitje");
        i.putExtra(android.content.Intent.EXTRA_TEXT, "Ik heb " + textViewName.getText() + " bezocht! \ndankzij de " + getResources().getString(R.string.app_name) + " app");
        startActivity(Intent.createChooser(i,"Uitje delen"));
    }

    //onclick methods for xml
    public void giveDownVote(View view)
    {
        if (network.isOnline()){
            new giveDownVoteThread().execute();
        }

    }

    public void giveUpVote(View view)
    {
        if (network.isOnline()) {
            new giveUpVoteThread().execute();
        }

    }



    class LoadDetailsSuggestedUitjes extends AsyncTask<String, String, String> {

        /*
                * Voordat we de taak starten laten we netjes een "zandloper" zien
        */

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RateUitjeItem.this);
            pDialog.setMessage("Details worden opgehaald... even geduld!");
            pDialog.setTitle("Detail laden");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }

        /*
                * getting All from url
        */

        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL

            String final_url = url_get_details + id_detail;

            JSONObject json = null;
            try {
                json =jParser.makeHttpRequest(final_url, "GET", params);

            }catch (Exception e){
                e.printStackTrace();
            }
            if (json == null) {
                Log.d("jsonechek", "jsonempty");
            }
            // Check your log cat for JSON reponse
            Log.d("Details: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    uitjes = json.getJSONArray(TAG_UITJES);

                    JSONObject x = uitjes.getJSONObject(0);
                    // Storing each json item in variable
                    naam = x.getString(TAG_NAME);
                    categorie = x.getString(TAG_CATEGORIE);
                    beschrijving = x.getString(TAG_BESCHRIJVING);
                    stad = x.getString(TAG_STAD);
                    straat = x.getString(TAG_STRAAT);
                    postcode = x.getString(TAG_POSTCODE);
                    telefoon = x.getString(TAG_TELEFOON);
                    weertype = x.getString(TAG_WEERTYPE);
                    coordinaat = x.getString(TAG_COORDINAAT);
                    upVotes = x.getInt(TAG_UPVOTECOUNT);
                    downVotes = x.getInt(TAG_DOWNVOTECOUNT);



                    HashMap<String, String> map = new HashMap<String, String>();

                    // adding each child node to HashMap key => value
                    map.put(TAG_NAME, naam);
                    map.put(TAG_CATEGORIE, categorie);
                    map.put(TAG_BESCHRIJVING, beschrijving);
                    map.put(TAG_STAD, stad);
                    map.put(TAG_STRAAT, straat);
                    map.put(TAG_POSTCODE, postcode);
                    map.put(TAG_TELEFOON, telefoon);
                    map.put(TAG_WEERTYPE, weertype);
                    map.put(TAG_UPVOTECOUNT, upVotes+"");
                    map.put(TAG_DOWNVOTECOUNT, downVotes+"");
                    map.put(TAG_COORDINAAT, coordinaat);
                    // adding HashList to ArrayList
                    uitjesList.add(map);


                    totalVotes = upVotes + downVotes;
                } else {
                    // no products found
                    Log.d("Uitjes status", "Details ophalen mislukt");
                    //set the textview to visible

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /*
                * After completing background task Dismiss the progress dialog
        * */

        protected void onPostExecute(String file_url)
        {

            Handler handler1 = new Handler();
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pDialog.setProgress(6);
                    textViewBeschrijving.setText(uitjesList.get(0).get(TAG_BESCHRIJVING));
                    textViewCategorie.setText(uitjesList.get(0).get(TAG_CATEGORIE));
                    if (uitjesList.get(0).get(TAG_WEERTYPE).equals("Sunny")) {
                        textViewWeertype.setBackground(getResources().getDrawable(R.drawable.ic_brightness_7_black_24dp));
                    }
                    else if (uitjesList.get(0).get(TAG_WEERTYPE).equals("Cloudy")){
                        textViewWeertype.setBackground(getResources().getDrawable(R.drawable.ic_cloud_queue_black_24dp));
                    }
                    else{
                        textViewWeertype.setBackground(getResources().getDrawable(R.drawable.light_rain50));
                    }
                    textViewTelefoon.setText(uitjesList.get(0).get(TAG_TELEFOON));
                    textViewStraat.setText(uitjesList.get(0).get(TAG_STRAAT));
                    textViewName.setText(uitjesList.get(0).get(TAG_NAME));
                    textViewPostcode.setText(uitjesList.get(0).get(TAG_POSTCODE));
                    textViewStad.setText(uitjesList.get(0).get(TAG_STAD));
                    double ups = upVotes + 0.0;
                    double downs = downVotes + 0.0;

                    String textpercentage;
                    if (ups > 0.0 && downs > 0.0) {
                        double percentage = ups / (ups + downs) * 100.0;
                        textpercentage = percentage + "% van de " + (upVotes + downVotes) + " personen vinden dit een leuk uitje";
                    }
                    else{
                        textpercentage = "Nog geen stemmen uitgebracht";
                    }
                    textViewPercentage.setText(textpercentage);

                    buttonVoteUp.setText(buttonVoteUp.getText() + " (" + uitjesList.get(0).get(TAG_UPVOTECOUNT)+ ")");
                    buttonVoteDown.setText(buttonVoteDown.getText() + " (" + uitjesList.get(0).get(TAG_DOWNVOTECOUNT)+ ")");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (uitjesList.get(0).get(TAG_TELEFOON).equals("")){
                                button_telefoonbutton.setVisibility(View.GONE);
                            }
                        }
                    });
                    pDialog.dismiss();
                }
            }, 5500);
            // dismiss the dialog after getting all products

            // updating UI from Background Thread

                    /*
                    * Updating parsed JSON data into textviews
                    */
            Log.d("TEST", uitjesList.size()+"");

        }
    }



    //
    //UPVOTE
    //

    class giveUpVoteThread extends AsyncTask<String, String, String>
    {
        /**
         * Voordat we de taak starten laten we netjes een "zandloper" zien
         */

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RateUitjeItem.this);
            pDialog.setMessage("Uw stem wordt verwerkt...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();


        }

        /**
         * getting All from url
         */
        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();



            if(!hasVoted)
            {
                upVotes++;
                totalVotes++;
                Log.d("totalvotes", totalVotes+ "");
                Log.d("upvotes", upVotes+ "");
                double upvotes = upVotes + 0.0;
                double x = upvotes / totalVotes * 100.0;
                Log.d("x", x + "");
                if (totalVotes >= 10 && x > 75)
                {
                    String newNaam = naam.replace(" ", "+");
                    String newBeschrijving = beschrijving.replace(" ", "+");
                    String newCategorie = categorie.replace(" ", "+");
                    String newtelefoon = telefoon.replace(" ", "+");
                    String newStraat = straat.replace(" ", "+");
                    String newPostcode = postcode.replace(" ", "+");
                    String newStad = stad.replace(" ", "+");
                    String newWeerType = weertype;
                    String parameters_url =

                            "NaamVar=" + newNaam + "&WeerTypeVar=" + newWeerType + "&BeschrijvingVar=" + newBeschrijving +
                                    "&CategorieVar=" + newCategorie + "&TelefoonVar=" + newtelefoon +
                                    "&StraatVar=" + newStraat + "&PostCodeVar=" + newPostcode +
                                    "&StadVar=" + newStad + "&CoordinaatVar=" + coordinaat;

                    final String insert_url = "http://coldfusiondata.site90.net/db_insert.php?" + parameters_url + "";
                    Log.d("String url", insert_url);
                    try
                    {
                        //ERRORS
                        jParser.ONLYURL(insert_url);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    //Eerst zetten we de suggestie in de uitjesdatabase,
                    //Daarna wordt de suggestie uit de suggestieDB verwijderd

                    final String delete_url = "http://coldfusiondata.site90.net/db_remove_suggestion.php?id=" + id_detail + "";
                    try
                    {
                        jParser.ONLYURL(delete_url);
                        //jParser.makeHttpRequestNoReturn(delete_url, "POST", params);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                }
                else
                {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    final String upvote_url = "http://coldfusiondata.site90.net/db_insert_upvote.php?id=" + id_detail + "";
                    Log.d("url", upvote_url);

                    jParser.ONLYURL(upvote_url);
                    //jParser.makeHttpRequestNoReturn(upvote_url, "POST", params);
                }

                hasVoted = true;

                double ups = upVotes + 0.0;
                double downs = downVotes + 0.0;
                double percentage = ups / (ups + downs) * 100.0;
                String textpercentage = percentage + "% van de " + (upVotes+downVotes) + " personen vinden dit een leuk uitje";

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttonVoteDown.setEnabled(false);
                        buttonVoteUp.setEnabled(false);
                        textViewPercentage.setText(textpercentage);
                        buttonVoteDown.setText(R.string.negUitje + " " + downVotes);
                        buttonVoteUp.setText(R.string.posUitje + " " + upVotes);
                    }
                });
            }
            else
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "U heeft al gestemd op dit uitje!", Toast.LENGTH_SHORT).show();
                    }
                });

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
        }
    }

    //
    //DOWNVOTE
    //

    class giveDownVoteThread extends AsyncTask<String, String, String> {

        /**
         * Voordat we de taak starten laten we netjes een "zandloper" zien
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RateUitjeItem.this);
            pDialog.setMessage("Uw stem wordt verwerkt...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }

        /**
         * getting All from url
         */

        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            if(!hasVoted)
            {
                downVotes++;
                totalVotes++;
                Log.d("totalvotes", totalVotes+ "");
                Log.d("downvotes", downVotes+ "");
                double downvotes = downVotes + 0.0;

                double x = (downvotes / totalVotes) * 100.0;
                Log.d("x", x + "");

                if (totalVotes >= 10 && x > 50) {
                    //delete
                    final String delete_url = "http://coldfusiondata.site90.net/db_remove_suggestion.php?id=" + id_detail;

                    jParser.ONLYURL(delete_url);

                }
                else
                {
                    final String downvote_url = "http://coldfusiondata.site90.net/db_insert_downvote.php?id=" + id_detail;

                    Log.d("url", downvote_url);
                    jParser.ONLYURL(downvote_url);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Stem succesvol uitgebracht", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                hasVoted = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttonVoteDown.setEnabled(false);
                        buttonVoteUp.setEnabled(false);
                    }
                });
            }
            else
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "U heeft al gestemd op dit uitje!", Toast.LENGTH_SHORT).show();
                    }
                });

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
        }
    }

    public void makeCallToUitje(View view){
        TextView x = (TextView)view;
        PhoneNumber = x.getText().toString();
        if (!PhoneNumber.equals("")) {
            AlertBuilderCall();
        }
    }

    public void makeCallToUitjeButton(View view){
        PhoneNumber = textViewTelefoon.getText().toString();
        if (!PhoneNumber.equals("")) {
            AlertBuilderCall();
        }
    }

    public void generateRoute(View view)
    {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + uitjesList.get(0).get(TAG_COORDINAAT).toString());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
        else {
            Toast.makeText(getApplicationContext(), "Kan google maps niet openen", Toast.LENGTH_SHORT).show();
        }

    }

    private DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    call(PhoneNumber);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:

                            Toast.makeText(RateUitjeItem.this, "Geannuleerd.", Toast.LENGTH_SHORT).show();


            }
        }
    };


    private void call(String number) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + number));
            startActivity(callIntent);
        } catch (ActivityNotFoundException e) {
            Log.e("Dialing example", "Call failed", e);
        }
    }

    private void AlertBuilderCall(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Doorgaan met bellen?").setMessage("Weet u het zeker? Er kunnen kosten van uw provider in rekening worden gebracht.").setIcon(17301543)
                .setPositiveButton("Ga door",dialogClickListener ).setNegativeButton("Annuleer", dialogClickListener);
        builder.show();
    }
}







