package com.thale.summeress.thale.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thale.summeress.thale.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class DisplayActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private String TAG = "DisplayActivity";
    private GoogleMap mMap;
    private LinkedHashMap<String, ArrayList<String>> displayInfo;

    private String sourceLocation;
    private LatLng source;
    private ArrayList<String> totalPoints;

    private ImageButton next;
    private ImageButton previous;
    private ImageButton change;

    private PolylineOptions options;

    private String exitInfo;

    private int index;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_display);
        init();

    }
    private void init(){
        displayInfo = new LinkedHashMap<>();
        totalPoints = new ArrayList<>();
        options = new PolylineOptions();

        next = (ImageButton)findViewById(R.id.next);
        previous = (ImageButton)findViewById(R.id.previous);
        change = (ImageButton)findViewById(R.id.changeToInner);

        next.setOnClickListener(this);
        previous.setOnClickListener(this);
        change.setOnClickListener(this);

        index = 0;

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
//        Bundle bundle = getIntent().getExtras();
//        String info = bundle.getString("displayInfo");
        String info = sharedPreferences.getString(getString(R.string.display_info), "");
        exitInfo = sharedPreferences.getString(getString(R.string.exit_info), "");
        Log.i(TAG, "exitInfo"+exitInfo);
        Gson gson = new Gson();
        Type entityType = new TypeToken<LinkedHashMap<String, ArrayList<String>>>(){}.getType();
        displayInfo = gson.fromJson(info, entityType);
        Log.i(TAG, "displayInfo"+displayInfo.toString());
        Iterator<ArrayList<String>> it = displayInfo.values().iterator();
        while (it.hasNext()){
            ArrayList<String> points= it.next();
            for (int i=0;i<points.size();i++){
                String point = points.get(i).replace("-",",");
                totalPoints.add(point);
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.previous:
                Log.i(TAG, "click previousBtn, index "+index);
                if(checkIndex(index-1)){
                    index--;
                    display();
                }
                break;

            case R.id.next:
                Log.i(TAG, "click nextBtn, index "+index);
                if(checkIndex(index+1)){
                    index++;
                    display();
                }
                break;
            case R.id.changeToInner:
                Log.i(TAG, "click changeBtn");
                Log.i(TAG, exitInfo);
                if (exitInfo.equals("")){
                    Toast.makeText(this, "You are not in subway station and are not allowed to access the inner station", Toast.LENGTH_LONG).show();
                    return;
                }else {
                    Intent intent = new Intent(DisplayActivity.this, InnerStationActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("Activity", "Display");
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
        }
    }

    private Boolean checkIndex(int i){
        if (i < 1){
            Toast.makeText(this, "This is the first step", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(i >= displayInfo.size()){
            Toast.makeText(this, "This is the last step", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void display(){
        options = new PolylineOptions();
        if (index==1) {
            options.add(source);
        }
        Iterator<String> it = displayInfo.keySet().iterator();
        while (it.hasNext()) {
            Boolean first = true;
            LatLng firstPoint = source;
            LatLng lastPoint = source;
            String key = it.next();
            if (key.contains(String.valueOf(index))) {
                ArrayList<String> points = displayInfo.get(key);
                for (int i = 0; i < points.size(); i++) {
                    String point = points.get(i).replace("-", ",");
                    LatLng l = new LatLng(
                            Double.parseDouble(point.split(",")[0]),
                            Double.parseDouble(point.split(",")[1])
                    );
                    if (first) {
                        firstPoint = l;
                        first = false;
                    }
                    lastPoint = l;
                    options.add(l).width(15);
                }
                LatLng focus = new LatLng(
                        (firstPoint.latitude + lastPoint.latitude) / 2.,
                        (firstPoint.longitude + lastPoint.longitude) / 2.);
                if (key.contains("WALKING")) {
                    Log.i(TAG, "walking");
                    Log.i(TAG, "focus" + focus.toString());
                    options.color(this.getResources().getColor(R.color.slateBlue));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(focus, 17));
                    Toast.makeText(this, "WALKING", Toast.LENGTH_SHORT).show();
                } else if (key.contains("TRANSIT")) {
                    options.color(this.getResources().getColor(R.color.dodgerBlue));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(focus, 13));
                    Toast.makeText(this,"TRANSIT", Toast.LENGTH_SHORT).show();
                }
                mMap.addPolyline(options);
                break;
            }

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
        Log.d(TAG, "onMapReady");
        mMap = googleMap;

        sourceLocation = displayInfo.get("0source").get(0);
        source = new LatLng(Double.parseDouble(sourceLocation.split(",")[0]),
                Double.parseDouble(sourceLocation.split(",")[1]));

        // Add a marker in Sydney and move the camera
        mMap.addMarker(new MarkerOptions().position(source).title("You Are Here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, 16));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        options = new PolylineOptions();
        for (int i=0; i<totalPoints.size();i++){
            LatLng point = new LatLng(
                    Double.parseDouble(totalPoints.get(i).split(",")[0]),
                    Double.parseDouble(totalPoints.get(i).split(",")[1])
            );
            options.add(point);
        }
        options.width(15).color(this.getResources().getColor(R.color.indianRed));
        mMap.addPolyline(options);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(DisplayActivity.this, ResultActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }
}
