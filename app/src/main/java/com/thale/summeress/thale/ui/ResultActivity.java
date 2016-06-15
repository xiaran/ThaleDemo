package com.thale.summeress.thale.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.thale.summeress.thale.R;
import com.thale.summeress.thale.network.ExitInfoTask;
import com.thale.summeress.thale.network.RouteInfo;
import com.thale.summeress.thale.tools.Path;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.thale.summeress.thale.network.RouteInfo.getRouteInfo;

public class ResultActivity extends Activity implements AdapterView.OnItemClickListener{

    private String TAG = "ResultActivity";

    private Context context;

    private String curLocation;
    private String dest;
    private String firstWalk;
    private String lastWalk;

    private String station;
    private String outStation;
    private String geocodeName;
    private String exitInfo;
    private String mUrl;

    private static Map<String, ArrayList<String>> outerRouteInfo;
    private List<Path> pathList;
    private ListView routeListView;
    private BaseAdapter adapter;
    private int[] imageID;

    private static LinkedHashMap<String, ArrayList<String>> displayInfo;
    private ArrayList<String> pointsInfo;
    private String lat;
    private String lng;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private boolean inStation = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_result);
        init();
    }

    private void init(){

        context = ResultActivity.this;

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        curLocation = sharedPreferences.getString(getString(R.string.current_location), "");
        curLocation = curLocation.replace("-", ",");

        dest = sharedPreferences.getString(getString(R.string.destination), "");
        station = sharedPreferences.getString(getString(R.string.station_name), "");
        geocodeName = sharedPreferences.getString(getString(R.string.geocode_name), "");
        firstWalk = "";
        exitInfo = "";
        imageID = new int[]{
                R.drawable.duration,
                R.drawable.walking,
                R.drawable.mtr,
                R.drawable.bus,
                R.drawable.exit};
        pathList = new ArrayList<>();
        routeListView = (ListView) findViewById(R.id.displayRoute);
        routeListView.setOnItemClickListener(this);

        adapter = new RouteAdapter();
        adapter.notifyDataSetChanged();

        displayInfo = new LinkedHashMap<>();
        pointsInfo = new ArrayList<>();

        pointsInfo.add(curLocation);
        displayInfo.put(displayInfo.size()+"source", pointsInfo);
        getRouteInfo(curLocation, dest, context, new RouteInfo.VolleyCallback() {
            @Override
            public void onSuccess(Map result) {
                outerRouteInfo = result;
                Log.i(TAG, result.toString());
                extractInfo();
                routeListView.setAdapter(adapter);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        routeListView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick");
        Gson gson = new Gson();
        String info = gson.toJson(displayInfo);
        editor.putString(getString(R.string.display_info), info);
        editor.putString(getString(R.string.exit_info), exitInfo);
        editor.commit();

        if (pathList.get(position).getDetails().contains("Take Exit")){
            Intent intent = new Intent(ResultActivity.this, InnerStationActivity.class);
            Bundle bundle = new Bundle();
//            bundle.putString("displayInfo", info);
            bundle.putString("Activity", "Result");
            intent.putExtras(bundle);
            startActivity(intent);
        }else {
            Log.i(TAG, "displayInfo " + displayInfo.toString());
            Intent intent = new Intent(ResultActivity.this, DisplayActivity.class);
            startActivity(intent);
        }
    }

    private String getUrl(String flag){
        String d;
        String source;
        if (flag.equals("first")){
            d = firstWalk;
            source = "22.3156009,114.2622043";
        }
        else{
            d = lastWalk;
            source = curLocation;
        }
        String url = "https://www.google.com/maps/dir/"+
                source+ "/"+ d +
                "/data=!3m1!4b1!4m2!4m1!3e3?hl=en&hl=en";
        return url;
    }

    private void extractInfo(){
        for (Map.Entry<String, ArrayList<String>> entry : outerRouteInfo.entrySet()){
            String key = entry.getKey();
            ArrayList<String> value = entry.getValue();
            Log.i(key, value.toString());
            if (Character.isDigit(key.charAt(0))){
                int id = Character.getNumericValue(key.charAt(0));
                switch(id){
                    case 0:
                        if (key.contains("FIRSTWALKING") || key.contains("WALKING")){
                            firstWalk = value.get(0).split("-")[0]+","+value.get(0).split("-")[1];
                            Log.i(TAG, "firstWalk "+firstWalk);
                            Log.i(TAG, "geocodeName "+geocodeName);
                            Log.i(TAG, "stationName "+station);
                            if (geocodeName.equals(station) && !firstWalk.equals("") && !value.toString().contains("Station")) {
                                mUrl = getUrl("first");
                                ExitInfoTask task = new ExitInfoTask(ResultActivity.this, station);
                                try {
                                    exitInfo = task.execute(mUrl).get();
                                    Log.i(TAG, "exitInfo "+exitInfo);
                                    if (!exitInfo.equals("Match Failed")) {
                                        editor.putString(getString(R.string.exit_info), exitInfo);
                                        editor.commit();
                                        Path path = new Path(imageID[4], "Take Exit " + exitInfo + " From "+station);
                                        pathList.add(path);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            String info = "";
                            pointsInfo = new ArrayList<>();
                            for (int i = 0; i < value.size(); i++){
                                info += value.get(i).split("-")[2] + "\n";
                                lat = value.get(i).split("-")[0];
                                lng = value.get(i).split("-")[1];
                                pointsInfo.add(lat + "-" + lng);
                            }
                            displayInfo.put(displayInfo.size()+"WALKING", pointsInfo);
                            info = info.substring(0, info.length()-1);
                            Log.i("info", info);
                            Path path = new Path(imageID[1], info);
                            pathList.add(path);
                        } else{
                            classify(key, value);
                        }
                        break;
                    default:
                        classify(key, value);
                }
            }else{
                Path path = new Path(imageID[0], value.get(0));
                pathList.add(path);
            }
        }
    }

    private void classify(String key, ArrayList<String> value){
        Path path;
        if (key.contains("NOTHING")){
            return;
        }
        else if (key.contains("TRANSIT")){
            String arrival = value.get(0);
            String departure = value.get(1);
            String start_location = value.get(3);
            String end_location = value.get(2);
            String line = value.get(4);

            if (arrival.contains("Station") && departure.contains("Station")){
                inStation = true;
                outStation = arrival;
            }

            if (line.matches(".*\\d+.*")) {
                path = new Path(imageID[3], "From: "+ departure + "\n" +
                        "To:      " + arrival + "\n" +
                        "Line:   " + line);
            } else{
                path = new Path(imageID[2], "From: " + departure + "\n" +
                        "To:      " + arrival + "\n" +
                        "Line:   " + line);
            }
            pathList.add(path);

            pointsInfo = new ArrayList<>();
            pointsInfo.add(start_location);
            pointsInfo.add(end_location);
            displayInfo.put(displayInfo.size()+"TRANSIT", pointsInfo);
        }
        else if (key.contains("WALKING")){
            Set set = new LinkedHashSet();
            String info;
            pointsInfo = new ArrayList<>();
            for (int i = 0; i < value.size(); i++){
                info = value.get(i).split("-")[2];
                lat = value.get(i).split("-")[0];
                lng = value.get(i).split("-")[1];
                set.add(info);
                pointsInfo.add(lat+"-"+lng);
            }
            displayInfo.put(displayInfo.size()+"WALKING", pointsInfo);
            Log.i(TAG, set.toString());
            info = TextUtils.join("\n", set);
            path = new Path(imageID[1], info);

            if (inStation){
                inStation = false;
                lastWalk = value.get(0).split("-")[0]+","+value.get(0).split("-")[1];
                mUrl = getUrl("last");
                ExitInfoTask task = new ExitInfoTask(ResultActivity.this, outStation);
                try {
                    exitInfo = task.execute(mUrl).get();
                    if (!exitInfo.equals("Match Failed")) {
                        editor.putString(getString(R.string.exit_info), exitInfo);
                        editor.commit();
                        Path exit = new Path(imageID[4], "Take Exit " + exitInfo + " From "+outStation);
                        pathList.add(exit);
                    }else{
                        editor.putString(getString(R.string.exit_info),"");
                        editor.commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            pathList.add(path);
        }
    }

    private class RouteAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return pathList.size();
        }

        @Override
        public Object getItem(int position) {
            return pathList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View layout = View.inflate(ResultActivity.this, R.layout.item_listview, null);

                ImageView imageView = (ImageView) layout.findViewById(R.id.mode);
                TextView textView = (TextView) layout.findViewById(R.id.details);
                Path path = pathList.get(position);
                imageView.setImageResource(path.getImageID());
                textView.setText(path.getDetails());
            return layout;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }
}
