package com.thale.summeress.thale.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.thale.summeress.thale.R;

import java.util.Date;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, View.OnClickListener {

    private String TAG = "MapActivity";

    private GoogleMap mMap;
    private String curLocation;
    private Location mLocation;
    private Marker marker;

    public static LatLng dest;
    public String destination;

    private ImageButton homeBtn;
    private static Context context;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        initUI();

        sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mLocation = new Location("fused");

    }

    private void initUI(){
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        context = MapActivity.this;

        homeBtn = (ImageButton) findViewById(R.id.homeBtn);
        homeBtn.setOnClickListener(this);

        mMap = ((SupportMapFragment)getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        editor.putString(getString(R.string.destination), "");
        editor.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.homeBtn:
                Log.i(TAG, "Click homeBtn");
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void onMapClick(LatLng arg0) {
        marker.remove();

        dest = arg0;
        destination = String.valueOf(dest.latitude)+","+String.valueOf(dest.longitude);
        Location location = new Location("");
        location.setLatitude(dest.latitude);
        location.setLongitude(dest.longitude);
        location.setTime(new Date().getTime());

        Log.i("markerLatitude", String.valueOf(location.getLatitude()));
        Log.i("markerLongitude", String.valueOf(location.getLongitude()));

        marker = mMap.addMarker(new MarkerOptions()
                .position(arg0)
                .draggable(true)
                .title("Marker")
        );

        String message = "Do you want to go to this place?";
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Important Note!");
        builder.setMessage(message);
        builder.setPositiveButton("Confirm It", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(context, ResultActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                dialog.dismiss();
                editor.putString(getString(R.string.destination), destination);
                editor.commit();
                context.startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();

    }
    public void showMap(Location mLocation){
        if (mLocation != null) {
            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);

            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            marker = mMap.addMarker(new MarkerOptions().position(latLng).title("You are here"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "onMapReady");
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissions for locating are not enabled!");
            return;
        }
        mMap.setMyLocationEnabled(true);
        curLocation = sharedPreferences.getString(getString(R.string.current_location), "");
        Log.i("curLocation", curLocation);
        if (curLocation.equals("")){
            Toast.makeText(this, "can not access your current location, Please check your GPS setting.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }
        String[] parts = curLocation.split("-");
        mLocation.setLatitude(Double.parseDouble(parts[0]));
        mLocation.setLongitude(Double.parseDouble(parts[1]));
        Log.i("mLocation", mLocation.toString());
        showMap(mLocation);
        mMap.setOnMapClickListener(this);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

}
