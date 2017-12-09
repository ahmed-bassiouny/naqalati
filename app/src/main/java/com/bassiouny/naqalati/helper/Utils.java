package com.bassiouny.naqalati.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.widget.ImageView;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeErrorDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeWarningDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.bumptech.glide.Glide;
import com.bassiouny.naqalati.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

/**
 * Created by bassiouny on 10/11/17.
 */

public class Utils {
    static AwesomeWarningDialog awesomeWarningDialog;
    static AwesomeErrorDialog awesomeErrorDialog;
    private static ACProgressFlower dialog;


    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public static String convertPhoneToEmail(String phone) {
        return phone + "@gmail.com";
    }

    public static void showWarningDialog(Context context, String msg) {
        if (awesomeWarningDialog == null)
            awesomeWarningDialog = new AwesomeWarningDialog(context);

        awesomeWarningDialog.setMessage(msg)
                .setColoredCircle(R.color.dialogNoticeBackgroundColor)
                .setDialogIconAndColor(R.drawable.ic_notice, R.color.white)
                .setCancelable(true)
                .show();
    }

    public static void showErrorDialog(Context context, String msg) {
        if (awesomeErrorDialog == null)
            awesomeErrorDialog = new AwesomeErrorDialog(context);

        awesomeErrorDialog.setMessage(msg)
                .setColoredCircle(R.color.dialogErrorBackgroundColor)
                .setDialogIconAndColor(R.drawable.ic_dialog_error, R.color.white)
                .setCancelable(true)
                .show();
    }

    public static void ContactSuppot(final Activity activity) {
        AwesomeErrorDialog awesomeErrorDialog = new AwesomeErrorDialog(activity);
        awesomeErrorDialog.setMessage(activity.getString(R.string.contact_suppoty))
                .setColoredCircle(R.color.dialogErrorBackgroundColor)
                .setDialogIconAndColor(R.drawable.ic_dialog_error, R.color.white)
                .setCancelable(true)
                .setButtonText(activity.getString(R.string.yes))
                .setButtonBackgroundColor(R.color.red_logo)
                .setButtonTextColor(R.color.white)
                .setErrorButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        activity.finish();
                    }
                })
                .show();
    }
    public static void showImage(Context context, String url, ImageView imageView){
        if(context==null)
            return;
        Glide.with(context).load(url)
                .into(imageView);
    }
    public static boolean isGpsEnable(Context context){
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    public static void showDialog(Context context) {
        if (dialog == null) {
            dialog = new ACProgressFlower.Builder(context)
                    .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                    .themeColor(Color.WHITE)
                    .text("من فضلك انتظر")
                    .fadeColor(Color.DKGRAY).build();
        }
        dialog.show();
    }

    public static void dismissDialog() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    public static String getCurrentDate(){
        //                              day-months-year
        // this method return date today like 2017-2-19
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        /*LocalDateTime now = LocalDateTime.now();*/
        return dateFormat.format(Calendar.getInstance().getTime());
    }
}
