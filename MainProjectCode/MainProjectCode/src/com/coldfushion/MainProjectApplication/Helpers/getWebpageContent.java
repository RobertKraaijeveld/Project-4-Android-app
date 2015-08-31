package com.coldfushion.MainProjectApplication.Helpers;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

/**
 * Created by ceesjan on 10-6-2015.
 */
public class getWebpageContent {
    public String openingstijden = "";
    public String Phonenumber = "";

    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            for (String url : urls) {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                try {
                    HttpResponse execute = client.execute(httpGet);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(
                            new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("resultaat: ", result);
            String Result = result.replace("  ", " ");
            int x = Result.indexOf("weekday_text");
            if (x > 0) {
                String FromweekdayText = Result.substring(x);
                String FromweekdayTextCleaned  = FromweekdayText.replace("      ", " ");
                int endOfWeekText = FromweekdayTextCleaned.indexOf("]");
                String OnlyWeekText = FromweekdayTextCleaned.substring(0, endOfWeekText);
                Log.d("onlyweek text", OnlyWeekText);
                String Enters = OnlyWeekText.replace("\", \"", "\n");
                int startReworked = Enters.indexOf(": [ \"");
                String TotalWeektext = Enters.substring(startReworked + 4);
                String CleanTotalWeektext = TotalWeektext.replace("\"", "\n");
                Log.d("resultaat", CleanTotalWeektext);

                openingstijden = CleanTotalWeektext;
            }
            else {
                Log.d("Openingstijden", "Geen tijden bekend");
                openingstijden = "Geen openingstijden bekend";
            }
            int y = Result.indexOf("formatted_phone_number");
            if (y > 0){
                String FromPhonenumber = Result.substring(y);
                int endofphonenumber = FromPhonenumber.indexOf("\",   \"");
                String PhonenumberTotal = FromPhonenumber.substring(0, endofphonenumber);
                String Phonenumber1 = PhonenumberTotal.replace(" ", "");
                int removesentences = Phonenumber1.lastIndexOf("\"");
                Phonenumber = Phonenumber1.substring(removesentences);

            }
            else {
                Phonenumber = "";
            }
        }
    }

    public void readWebpage(String url) {
        DownloadWebPageTask task = new DownloadWebPageTask();
        Log.d("url", url);
        task.execute(new String[]{url});
    }


}