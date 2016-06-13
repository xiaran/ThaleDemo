package com.thale.summeress.thale.service;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.thale.summeress.thale.ui.HomeActivity;
import com.thale.summeress.thale.ui.WelcomeActivity;

public class LocationTraceService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final IBinder binder = new LocationTraceBinder();
    private static String TAG = "locationTraceService";

    LocationManager locationManager;
    private boolean isGpsEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean isLocating = false;

    private boolean isLocationRequestSuspend = true;

    private static boolean first = true;
    private static  long INTERVAL = 0;
    private static  long FAST_INTERVAL = 60 * 1000;
    private LocationRequest mLocationRequest;

    private GoogleApiClient mGoogleApiClient;

    private String activity;

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");

    }

    private void CheckTowerAndGpsStatus() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isNetworkEnabled && !isGpsEnabled)
        {
            Location nLocation = new Location("no");
            nLocation.setLatitude(0);
            nLocation.setLongitude(0);
            handleNewLocation(nLocation);
        }

    }

    private void init(){
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(INTERVAL)
                .setFastestInterval(FAST_INTERVAL);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        Bundle bundle = intent.getExtras();
        activity = bundle.getString("Activity");
        Log.i(TAG, activity);
        CheckTowerAndGpsStatus();
        if (isNetworkEnabled && isGpsEnabled) {
            init();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, activity);
        Log.d(TAG, "GpsLoggingService is being destroyed by Android OS.");
        if (mGoogleApiClient!=null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (isLocationRequestSuspend) {
            Log.i(TAG, "Location services connected");
            isLocationRequestSuspend = false;
            startLocating();
        }
    }

    private void startLocating(){
        if (!mGoogleApiClient.isConnected()){
            isLocationRequestSuspend = true;
            return;
        }
        if (!isLocating) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissions for locating are not enabled!");
                Location l = new Location("no");
                l.setLatitude(0.1);
                l.setLongitude(0.1);
                handleNewLocation(l);
                return;
            }
        }
        isLocating = true;
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    public void stopLocating() {
        Log.i(TAG, "GpsLoggingService.StopLocating");
        stopForeground(true);
        isLocating = false;
        isLocationRequestSuspend = false;

        if (mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    void RestartGpsManagers() {
        Log.d(TAG, "GpsLoggingService.RestartGpsManagers");
        stopLocating();
        mGoogleApiClient.disconnect();
        mGoogleApiClient.connect();
        startLocating();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnected.");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        Log.d(TAG, location.toString());
        handleNewLocation(location);
        if (first){
            INTERVAL = 60 * 1000;
            mLocationRequest.setInterval(INTERVAL);
            first = false;
        }
    }

    private void handleNewLocation(Location location){
        Message msg = new Message();
        String message = String.valueOf(location.getLatitude())+"-"+String.valueOf(location.getLongitude());
        msg.obj = message;
        Log.i(TAG, "location "+location.getLatitude()+" "+location.getLongitude());
        Log.i(TAG, "activity "+activity);
        if (activity.equals("Welcome")) {
            WelcomeActivity.mHandler.handleMessage(msg);
        }
        else if(activity.equals("Home")) {
            HomeActivity.mHandler.handleMessage(msg);
        }
    }
    @Override
    public void onLowMemory() {
        Log.d(TAG, "Android is low on memory.");
        super.onLowMemory();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, connectionResult.getErrorMessage());
    }

    public class LocationTraceBinder extends Binder {
        public LocationTraceService getService() {
            return LocationTraceService.this;
        }
    }

}
