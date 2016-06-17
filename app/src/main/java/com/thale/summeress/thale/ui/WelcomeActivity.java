package com.thale.summeress.thale.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.thale.summeress.thale.R;
import com.thale.summeress.thale.network.GeocodeInfo;
import com.thale.summeress.thale.network.StationInfo;
import com.thale.summeress.thale.service.LocationTraceService;

import static com.thale.summeress.thale.network.GeocodeInfo.getGeocodeName;
import static com.thale.summeress.thale.network.StationInfo.getStationName;

public class WelcomeActivity extends Activity {

    private static final String TAG = "WelcomeActivity";

    private Location mLocation;
    private String curLocation;
    private String stationName;
    private String geocodeName;

    private Intent serviceIntent;
    public static Handler mHandler;

    public static Context context;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_welcome);
        init();
    }

    private void init(){
        stationName = "";
        geocodeName = "";
        mLocation = new Location("0,0");
        mHandler = new IncomingHandler();
        context = WelcomeActivity.this;
        sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
        );
        editor = sharedPreferences.edit();
        serviceIntent = new Intent(this, LocationTraceService.class);
    }


    public boolean isOnline() {
        connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        Bundle bundle = new Bundle();
        bundle.putString("Activity","Welcome");
        serviceIntent.putExtras(bundle);
        startService(serviceIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        stopService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }


    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            curLocation = msg.obj.toString();
            Log.i(TAG, curLocation);
            String[] parts = curLocation.split("-");
            double latitude = Double.parseDouble(parts[0]);
            double longitude = Double.parseDouble(parts[1]);
            editor.putString(context.getString(R.string.current_location), curLocation);
            editor.commit();
            mLocation.setLatitude(latitude);
            mLocation.setLongitude(longitude);
            if (isOnline()) {
                getStationName(mLocation, context, new StationInfo.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.i(TAG, "onSuccess");
                        Log.i(TAG, "stationName " + result);
                        stationName = result;
                        getGeocodeName(mLocation, context, new GeocodeInfo.VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {
                                Log.i(TAG, "onSuccess");
                                Log.i(TAG, "geocodeName " + result);
                                geocodeName = result;
                                editor.putString(context.getString(R.string.current_location), curLocation);
                                editor.putString(context.getString(R.string.station_name), stationName);
                                editor.putString(context.getString(R.string.geocode_name), geocodeName);
                                editor.commit();
                                try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e) {
                                }
                                Intent intent = new Intent(context, HomeActivity.class);
                                context.startActivity(intent);
                            }
                        });
                    }
                });
            }else {
                try {
                    editor.putString(context.getString(R.string.station_name), stationName);
                    editor.putString(context.getString(R.string.geocode_name), geocodeName);
                    editor.commit();
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                }
                Intent intent = new Intent(context, HomeActivity.class);
                context.startActivity(intent);
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }
}
