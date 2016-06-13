package com.thale.summeress.thale.network;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by summeress on 16/6/4.
 */
public class ExitInfoTask extends AsyncTask<String, Void, String>{
    private String TAG = "ExitInfoTask";

    private String mResponse;
    private String mStationName;
    private String exitInfo;
    private Context mContext;
    private ProgressDialog progressDialog;

    public ExitInfoTask(Context context, String station){
        mContext = context;
        mStationName = station;
    }
    protected void onPreExecute() {
        Log.i(TAG, "onPreExecute()");
        if (progressDialog != null){
            Log.i(TAG, "not null");
            progressDialog.dismiss();
            progressDialog = null;
        }
        progressDialog = new ProgressDialog(mContext);
        this.progressDialog.setMessage("Path Planning now, please wait...");
        this.progressDialog.show();
    }

    @Override
    protected String doInBackground(String... urls) {
        Log.i(TAG, "doInBackground()");
        try{
            Log.i(TAG, "myUrl "+urls[0]);
            mResponse = downloadUrl(urls[0]);
            Pattern pattern = Pattern.compile("Take\\sexit\\s"+mStationName + "\\sExit\\s([A-Z]\\d?)");
            Matcher matcher = pattern.matcher(mResponse);
            if (matcher.find()) {
                exitInfo = matcher.group(1);
                matcher.group();
            } else {
                exitInfo = "Match Failed";
            }
            Log.i(TAG, exitInfo);
            return exitInfo;
        } catch (IOException e){
            return "Unable to retrieve web page. URL may be invalid";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Log.i(TAG, "onPostExecute()");
        if (progressDialog != null){
            progressDialog.dismiss();
        }
        super.onPostExecute(result);
    }


    private String downloadUrl(String myUrl) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            is = conn.getInputStream();
            // Convert the InputStream into a string
            String contentAsString = readIt(is);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder response = new StringBuilder();
        char[] buffer = new char[65536];
        int read;
        while (true){
            read = reader.read(buffer);
            if (read == -1){
                break;
            }
            response.append(buffer);
        }
        return response.toString();
    }
}