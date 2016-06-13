package com.thale.summeress.thale.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

    private  static Location mLocation;
    private  static String curLocation;
    private  static String stationName;
    private  static String geocodeName;

    private  static Intent serviceIntent;
    public  static Handler mHandler;

    public static Context context;

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_welcome);
        init();
    }

    private void init(){
        mLocation = new Location("0,0");
        mHandler = new IncomingHandler();
        context = WelcomeActivity.this;
        sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
        );
        editor = sharedPreferences.edit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        serviceIntent = new Intent(this, LocationTraceService.class);
        Bundle bundle = new Bundle();
        bundle.putString("Activity","Welcome");
        serviceIntent.putExtras(bundle);
        startService(serviceIntent);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            } else {
                    Toast.makeText(context, "Permission Denied", Toast.LENGTH_LONG);
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

        }
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            curLocation = msg.obj.toString();
            Log.i(TAG, curLocation);
            String[] parts = curLocation.split("-");
            double latitude = Double.parseDouble(parts[0]);
            double longitude = Double.parseDouble(parts[1]);
            if (latitude==0 && longitude == 0) {
                Toast.makeText(context, "Please check your network and gps setting", Toast.LENGTH_SHORT).show();
                context.stopService(serviceIntent);
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            }
            else if(latitude==0.1 && longitude == 0.1){
                ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
            }
            mLocation.setLatitude(latitude);
            mLocation.setLongitude(longitude);
            getStationName(mLocation, context, new StationInfo.VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    Log.i(TAG, "onSuccess");
                    Log.i(TAG, "stationName " + result);
                    stationName = result;
                    getGeocodeName(mLocation, context, new GeocodeInfo.VolleyCallback(){
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
                            }catch (InterruptedException e){}
                            Intent intent = new Intent(context, HomeActivity.class);
                            context.startActivity(intent);
                        }
                    });
                }
            });

        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }
}
