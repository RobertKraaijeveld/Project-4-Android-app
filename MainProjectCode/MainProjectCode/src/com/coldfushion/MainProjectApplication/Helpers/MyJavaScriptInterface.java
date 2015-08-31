package com.coldfushion.MainProjectApplication.Helpers;

import android.content.Context;

/**
 * Created by ceesjan on 28-5-2015.
 */
public class MyJavaScriptInterface {
    private Context ctx;
    public String PlaceID = "";
    public String PlaceAdress = "";


    public MyJavaScriptInterface(Context ctx) {
        this.ctx = ctx;
    }

    @android.webkit.JavascriptInterface
    public void getHTML(String html) {
        PlaceID = html;

    }

    @android.webkit.JavascriptInterface
    public void GetHTML(String html) {
        PlaceAdress = html;
    }


    public String getPlaceID() {
        return PlaceID;
    }

    public String getPlaceAdress(){
        return PlaceAdress;
    }
}
