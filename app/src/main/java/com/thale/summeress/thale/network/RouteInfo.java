package com.thale.summeress.thale.network;

import android.app.ProgressDialog;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;

/**
 * Created by summeress on 16/6/8.
 */
public class RouteInfo {
    private static String TAG = "RouteInfo";
    private static Map<String, ArrayList<String>> outerRouteInfo;
    private static ArrayList<String> walkInfo = new ArrayList<>();
    private static ArrayList<String> transitInfo = new ArrayList<>();

    private static Boolean first = true;
    private static int totalStep = -1;

    private static ProgressDialog progressDialog;

    public static void getRouteInfo(String source, String dest, Context context, final VolleyCallback callback) {

        outerRouteInfo = new LinkedHashMap<>();

        if (source.equals("") || dest.equals("")) {
            return;
        }else {
            String myUrl = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + source +
                    "&destination=" + dest +
                    "&region=hk&mode=transit&transit_mode=subway&sensor=false" +
                    "&language=en&key=AIzaSyAfEV1GZo5lmK2d4XRpweQerVH3tUoNrHU";
            Log.i(TAG, "myUrl: "+myUrl);
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET, myUrl, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        String status = jsonObject.getString("status");
                        if (status.equals("OK")) {
                            Log.i(TAG, "status: "+status);
                            JSONArray results = jsonObject.getJSONArray("routes");
                            JSONObject routeInfo = results.getJSONObject(0);
                            JSONArray legs = routeInfo.getJSONArray("legs");
                            JSONObject legsInfo = legs.getJSONObject(0);
                            JSONObject duration = legsInfo.getJSONObject("duration");
                            String dur = duration.getString("text");
                            ArrayList<String> list = new ArrayList<>();
                            list.add(dur);
                            outerRouteInfo.put("duration", list);

                            JSONArray steps = legsInfo.getJSONArray("steps");
                            totalStep = steps.length();
                            for (int j = 0; j < totalStep; j++) {

                                JSONObject stepsInfo = steps.getJSONObject(j);
                                String travel_mode = stepsInfo.getString("travel_mode");
                                if (travel_mode.equals("WALKING") && first) {
                                    first = false;
                                    walkInfo = stepsDetails(stepsInfo);
                                    outerRouteInfo.put(String.valueOf(j)+"FIRSTWALKING", walkInfo);
                                    Log.i(TAG, outerRouteInfo.toString());
                                }
                                else if (travel_mode.equals("TRANSIT")){
                                    first = false;
                                    transitInfo = transitDetails(stepsInfo);
                                    outerRouteInfo.put(String.valueOf(j)+"TRANSIT", transitInfo);
                                    Log.i(TAG, outerRouteInfo.toString());
                                }
                                else if (travel_mode.equals("WALKING")){
                                    JSONObject stepDetails = stepsInfo.getJSONObject("distance");
                                    String distance = stepDetails.getString("text");
                                    if (distance.equals("1 m")){
                                        ArrayList<String> nothing = new ArrayList<>();
                                        nothing.add("");
                                        outerRouteInfo.put(String.valueOf(j)+"NOTHING", nothing);
                                        continue;
                                    } else {
                                        walkInfo = stepsDetails(stepsInfo);
                                        outerRouteInfo.put(String.valueOf(j)+"WALKING", walkInfo);
                                        Log.i(TAG, outerRouteInfo.toString());
                                    }
                                }
                            }
                        } else {
                            Log.i(TAG, "Status: "+status);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callback.onSuccess(outerRouteInfo);
                    if (progressDialog.isShowing()){
                        progressDialog.dismiss();
                        first = true;
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.i(TAG,"volleyError"+volleyError.getMessage());
                    if (progressDialog.isShowing()){
                        progressDialog.dismiss();
                        first = true;
                    }
                }
            });
            requestQueue.add(jsonObjectRequest);
            requestQueue.start();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Route Planning now, Please wait...");
            progressDialog.show();
        }
    }

    public interface VolleyCallback{
        void onSuccess(Map result);
    }

    public static ArrayList<String> stepsDetails(JSONObject stepsInfo)
    {
        ArrayList<String> walk = new ArrayList<>();
        String end;
        String instructions;
        try {
            JSONArray stepDetail = stepsInfo.getJSONArray("steps");
            for (int i = 0; i < stepDetail.length(); i++) {
                JSONObject step = stepDetail.getJSONObject(i);
                JSONObject end_location = step.getJSONObject("end_location");
                end = end_location.getString("lat") + "-" + end_location.getString("lng");
                if (step.has("html_instructions")) {
                    instructions = step.getString("html_instructions");
                } else {
                    instructions = stepsInfo.getString("html_instructions");
                }
                instructions = instructions.replaceAll("<b>|<\b>","");
                instructions = instructions.replaceAll("<.*?>"," ");
                end += "-" + instructions;
                walk.add(end);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        return walk;
    }

    public static ArrayList<String> transitDetails(JSONObject stepsInfo){
        ArrayList<String> transit = new ArrayList<>();
        try {
            JSONObject transitDetails = stepsInfo.getJSONObject("transit_details");
            JSONObject arrivalStop = transitDetails.getJSONObject("arrival_stop");
            JSONObject departureStop = transitDetails.getJSONObject("departure_stop");
            String arrival = arrivalStop.getString("name");
            String departure = departureStop.getString("name");
            String arrival_location = arrivalStop.getJSONObject("location").getString("lat") + "-" +
                    arrivalStop.getJSONObject("location").getString("lng");
            String departure_location = departureStop.getJSONObject("location").getString("lat") + "-" +
                    departureStop.getJSONObject("location").getString("lng");

            JSONObject lineDetails = transitDetails.getJSONObject("line");
            String line = lineDetails.getString("short_name");

            transit.add(arrival);
            transit.add(departure);
            transit.add(arrival_location);
            transit.add(departure_location);
            transit.add(line);

        } catch (JSONException e){
            e.printStackTrace();
        }
        Log.i(TAG, "transit"+transit.toString());
        return transit;
    }
}
