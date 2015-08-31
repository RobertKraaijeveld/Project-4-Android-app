package com.coldfushion.MainProjectApplication.Helpers;

import android.content.Context;
import com.coldfushion.MainProjectApplication.R;

/**
 * Created by ceesjan on 3-6-2015.
 */
public class MenuSelectItem {

    private Context context;
    private String[] mMenuItems;

    public MenuSelectItem(Context context){
        this.context = context;
    }

    public int nameToInt(int position){
        mMenuItems = context.getResources().getStringArray(R.array.menu_items);

        if(mMenuItems[position].toLowerCase().equals("locatie wijzigen")){return 1;}
        else if (mMenuItems[position].toLowerCase().equals("bekijk uitjes op kaart")){return 0;}
        else if (mMenuItems[position].toLowerCase().equals("alle uitjes")) {return 5;}
        else if (mMenuItems[position].toLowerCase().equals("suggestie maken")){return 3;}
        else if(mMenuItems[position].toLowerCase().equals("uitje beoordelen")){return 4;}
        return 0;
    }
}
