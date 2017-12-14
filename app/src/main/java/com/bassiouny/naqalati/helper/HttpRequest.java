package com.bassiouny.naqalati.helper;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by bassiouny on 14/12/17.
 */

public class HttpRequest {

    // this notification to driver app
    public static void sendNotify(Context context, String token) {
        if (token == null || token.isEmpty())
            return;
        String urlString = "http://naqlati.com/control/notification/notify.php?";
        String fullToken = "id=" + token;
        String fullBody = "body=" + "لديك_طلب_توصيل_جديد";
        String and = "&";

        String fullUrl = urlString + fullToken + and + fullBody;
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, fullUrl, null, null, null);
        queue.add(jsObjRequest);
    }
}
