package com.thale.summeress.thale.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.thale.summeress.thale.R;
import com.thale.summeress.thale.network.GeocodeInfo;
import com.thale.summeress.thale.network.StationInfo;
import com.thale.summeress.thale.service.LocationTraceService;

import static com.thale.summeress.thale.network.GeocodeInfo.getGeocodeName;
import static com.thale.summeress.thale.network.StationInfo.getStationName;
import static com.thale.summeress.thale.tools.checkService.checkPlayServices;

public class HomeActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "HomeActivity";
    private static boolean result;

    private ImageButton searchBtn;
    private ImageButton listBtn;

    private int selected = -1;
    private int backButtonCount = 0;

    private Location mLocation;
    private String curLocation;
    private String stationName;
    private String geocodeName;

    private Intent serviceIntent;
    public static Handler mHandler;

    public Context context;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    private LocationManager locationManager;
    private boolean isGpsEnabled = false;
    private boolean isNetworkEnabled = false;
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_home);

        mLocation = new Location("0,0");
        result = checkPlayServices(this);
        mHandler = new IncomingHandler();
        context = HomeActivity.this;

        sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
        );
        editor = sharedPreferences.edit();
        initUI();
        checkGpsStatus();
    }

    private void initUI() {
        searchBtn = (ImageButton) findViewById(R.id.searchBtn);
        listBtn = (ImageButton) findViewById(R.id.listBtn);

        searchBtn.setOnClickListener(this);
        listBtn.setOnClickListener(this);
    }

    private void checkGpsStatus(){
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.i(TAG, ""+isNetworkEnabled+" "+isGpsEnabled);
        if (!isGpsEnabled || !isNetworkEnabled){
            Log.i(TAG, "something");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Important Note!");
            builder.setMessage("Please open GPS");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    try {
                        startActivityForResult(intent, 0);
                    } catch (ActivityNotFoundException e) {
                        intent.setAction(Settings.ACTION_SETTINGS);
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void openNetworkConnection(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Important Note!");
        builder.setMessage("Please enable network access");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.android.phone","com.android.phone.NetworkSetting");
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchBtn:
                Log.i(TAG, "Click SearchBtn");
                if (isOnline()) {
                    Intent intent = new Intent(this, MapActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Can not access network, Please check your setting", Toast.LENGTH_SHORT);
                    openNetworkConnection();
                }
            break;
            case R.id.listBtn:
                Log.i(TAG, "Click ListBtn");
                showDialog();
                break;
        }
    }

    private void showDialog(){
        selected = -1;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please choose the desired one");
        final String[] choiceList = {
                "Tourist Service", "Lost Property Office", "Police Post",
                "Shop", "Customer Service Centre", "ATM"
        };
        builder.setSingleChoiceItems(choiceList, selected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selected = which;
                Log.i(TAG, ""+selected);
            }
        }).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(HomeActivity.this, InnerStationActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("Activity", "Home");
                        bundle.putString("Facility", String.valueOf(selected)+choiceList[selected]);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        if(backButtonCount >= 1)
        {
            stopService(serviceIntent);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        serviceIntent = new Intent(this, LocationTraceService.class);
        Bundle bundle = new Bundle();
        bundle.putString("Activity","Home");
        serviceIntent.putExtras(bundle);
        startService(serviceIntent);
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        if (isMyServiceRunning(LocationTraceService.class)){
            stopService(serviceIntent);
            serviceIntent = new Intent(this, LocationTraceService.class);
            Bundle bundle = new Bundle();
            bundle.putString("Activity","Home");
            serviceIntent.putExtras(bundle);
            startService(serviceIntent);
        }

    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        stopService(serviceIntent);
    }

    public boolean isOnline() {
        connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
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

            if (isOnline()) {
                mLocation.setLatitude(latitude);
                mLocation.setLongitude(longitude);
                getStationName(mLocation, context, new StationInfo.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.i(TAG, "onSuccess");
                        Log.i(TAG, "stationName " + result);
                        if (result.equals("Error")){
                            Toast.makeText(context, "can not access network, Please check your setting", Toast.LENGTH_SHORT);
                        }
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
                            }
                        });
                    }
                });
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

}

