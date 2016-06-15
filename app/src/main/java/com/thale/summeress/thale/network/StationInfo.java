package com.thale.summeress.thale.network;

import android.content.Context;
import android.location.Location;
import android.os.Message;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by summeress on 16/6/2.
 */
public class StationInfo {
    private static String TAG = "stationInfo";
    private static String stationName;

    public static void getStationName(Location location, Context context, final VolleyCallback callback) {
        stationName = "";

        if (location != null) {

            String myUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "location="+location.getLatitude() + "," + location.getLongitude() +
                    "&rankby=distance&distance=100" +
                    "&types=subway_station&language=en" +
                    "&key=AIzaSyAfEV1GZo5lmK2d4XRpweQerVH3tUoNrHU";
            Log.d(TAG, "myUrl " + myUrl);
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET, myUrl, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        String status = jsonObject.getString("status");
                        if (status.equals("OK")) {
                            Log.d("status: ", status);
                            JSONArray results = jsonObject.getJSONArray("results");
                            JSONObject stationInfo = results.getJSONObject(0);
                            stationName = stationInfo.getString("name");
                            Log.i(TAG, "stationName " + stationName);

                        }else {
                            Log.d("Status: ", status);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callback.onSuccess(stationName);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.d("volleyError", volleyError.getMessage());
                    callback.onSuccess("Error");
                }
            });
            requestQueue.add(jsonObjectRequest);
            requestQueue.start();
        } else {
        }
    }

    public interface VolleyCallback{
        void onSuccess(String result);
    }
}
