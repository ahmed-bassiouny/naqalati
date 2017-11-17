package com.bassiouny.naqalati.helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by bassiouny on 12/11/17.
 */

public class SharedPref {

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String nameKey = "nameKey";
    public static final String phoneKey = "phoneKey";
    public static final String imageKey = "imageKey";
    public static final String latKey = "latKey";
    public static final String lngKey = "lngKey";
    private static SharedPreferences sharedpreferences;

    private static void init(Context context){
        if(sharedpreferences==null)
            sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }
    public static void setInfoUser(Context context,String name,String phone,String imageUrl){
        init(context);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(nameKey,name);
        editor.putString(phoneKey,phone);
        editor.putString(imageKey,imageUrl);
        editor.commit();
    }
    public static void setLocationUser(Context context,Double lat,Double lng){
        init(context);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(latKey,String.valueOf(lat));
        editor.putString(lngKey,String.valueOf(lng));
        editor.commit();
    }
    public static String getUserName(Context context){
        init(context);
        return sharedpreferences.getString(nameKey,"");
    }
    public static String getPhone(Context context){
        init(context);
        return sharedpreferences.getString(phoneKey,"");
    }
    public static String getUserImage(Context context){
        init(context);
        return sharedpreferences.getString(imageKey,"");
    }
    public static String getUserLat(Context context){
        init(context);
        return sharedpreferences.getString(latKey,"0.0");
    }
    public static String getUserLng(Context context){
        init(context);
        return sharedpreferences.getString(lngKey,"0.0");
    }
}
