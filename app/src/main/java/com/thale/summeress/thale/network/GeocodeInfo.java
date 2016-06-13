package com.thale.summeress.thale.network;

/**
 * Created by summeress on 16/6/8.
 */

import android.content.Context;
import android.location.Location;
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
public class GeocodeInfo {
    private static String TAG = "geocodeInfo";
    private static String geocodeName;

    public static void getGeocodeName(Location location, Context context, final VolleyCallback callback) {
        geocodeName = "";

        if (location != null) {

            String myUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" +
                    location.getLatitude() + "," + location.getLongitude() +
                    "&sensor=true&language=en";
            Log.d(TAG, "myUrl " + myUrl);
            final RequestQueue requestQueue = Volley.newRequestQueue(context);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET, myUrl, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        String status = jsonObject.getString("status");
                        if (status.equals("OK")) {
                            Log.d("status: ", status);
                            JSONArray results = jsonObject.getJSONArray("results");
                            for (int j = 0; j < results.length(); j++) {
                                JSONObject components = results.getJSONObject(j);
                                if (components.getJSONArray("types").toString().contains("transit_station")){
                                JSONArray resultArray = components.getJSONArray("address_components");
                                    for (int i = 0; i < resultArray.length(); i++) {
                                        JSONObject object = resultArray.getJSONObject(i);
                                        JSONArray types = object.getJSONArray("types");
                                        if (types.toString().contains("point_of_interest")) {
                                            geocodeName = object.get("long_name").toString();
                                            Log.i(TAG, "geocodeName " + geocodeName);
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                        } else {
                            Log.d("Status: ", status);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callback.onSuccess(geocodeName);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.d("volleyError", volleyError.getMessage());
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

