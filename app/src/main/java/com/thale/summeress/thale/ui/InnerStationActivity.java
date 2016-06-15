package com.thale.summeress.thale.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.multidex.MultiDex;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.thale.summeress.thale.R;
import com.thale.summeress.thale.tools.ScaleImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InnerStationActivity extends Activity implements View.OnClickListener{

    private String TAG = "innerStationActivity";

    private ImageButton change;
    private ScaleImageView inner;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String activity;

    private String exitInfo;

    private Canvas mCanvas;
    private Paint mPaint;
    private Bitmap myImg;

    public Point curPos;
    public Point EXIT_A;
    public Point EXIT_B;
    public Point EXIT_C;
    public Point EXIT_D;
    public Point EXIT_E;
    public Point topLeft;
    public Point topRight;
    public Point bottomLeft;
    public Point bottomRight;

    public Point Tourist_Service;
    public Point Lost;
    public Point Police;
    public Point Shop;
    public Point Customer_Service;
    public Point ATM;

    public ArrayList<Point> RECT;
    public ArrayList<Point> EXIT;
    public ArrayList<Point> FACILITY;
    public Map<String, ArrayList<Integer>> TABLE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inner_station);

        Log.d(TAG, "onCreate");
        inner = (ScaleImageView) findViewById(R.id.inner);
        init();
    }

    private void init(){
        inner = (ScaleImageView) findViewById(R.id.inner);
        change = (ImageButton) findViewById(R.id.changeToOuter);

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        curPos = new Point(1015, 370);

        EXIT_A = new Point(813, 295);
        EXIT_B = new Point(281, 283);
        EXIT_C = new Point(799, 388);
        EXIT_D = new Point(1375, 508);
        EXIT_E = new Point(1440, 527);

        topLeft = new Point(862, 318);
        topRight = new Point(1344, 363);
        bottomLeft = new Point(807, 380);
        bottomRight = new Point(1292, 485);

        Tourist_Service = new Point(182, 220);
        Lost = new Point(220, 188);
        Police = new Point(267, 178);
        Shop = new Point(1073, 312);
        Customer_Service = new Point(1296, 340);
        ATM = new Point(1257, 318);

        RECT = new ArrayList<Point>(){{
            add(topLeft);
            add(topRight);
            add(bottomLeft);
            add(bottomRight);
        }};

        EXIT = new ArrayList<Point>(){{
            add(EXIT_A);
            add(EXIT_B);
            add(EXIT_C);
            add(EXIT_D);
            add(EXIT_E);
        }};

        FACILITY = new ArrayList<Point>(){{
            add(Tourist_Service);
            add(Lost);
            add(Police);
            add(Shop);
            add(Customer_Service);
            add(ATM);
        }};

        TABLE = new HashMap<String, ArrayList<Integer>>(){{
            put("A", new ArrayList<Integer>(){{add(1);add(3);add(2);}});
            put("B", new ArrayList<Integer>(){{add(3);add(4);}});
            put("C", new ArrayList<Integer>(){{add(3);add(1);add(4);}});
            put("D", new ArrayList<Integer>(){{add(4);add(2);add(3);}});
            put("E", new ArrayList<Integer>(){{add(4);add(3);}});
        }};
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        setImageAndPos();

        activity = getIntent().getExtras().getString("Activity");
        if (activity.equals("Result") || activity.equals("Display")){
            exitInfo = sharedPreferences.getString(getString(R.string.exit_info), "");
            if (!exitInfo.equals("")) {
                exitInfo = exitInfo.substring(0, 1);
                Log.i(TAG, "exitInfo " + exitInfo);
                change.setVisibility(View.VISIBLE);
                change.setOnClickListener(this);
                drawRoute(exitInfo);
            }
        }else if(activity.equals("Home")){
            String facility = getIntent().getExtras().getString("Facility");
            drawMarker(facility);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.changeToOuter:
                Log.i(TAG, "click changBtn");
                Intent intent = new Intent(InnerStationActivity.this, DisplayActivity.class);
                startActivity(intent);
        }
    }

    private void setImageAndPos(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inInputShareable = true;

        myImg = BitmapFactory.decodeResource(this.getResources(), R.drawable.inner_admiralty, options).copy(
                Bitmap.Config.RGB_565, true);
//        Matrix matrix = new Matrix();
//        Bitmap map = Bitmap.createBitmap(myImg, 0, 0, myImg.getWidth(), myImg.getHeight(),
//                matrix, true);
        Log.i("MyImg", myImg.getWidth()+","+myImg.getHeight());

        mCanvas = new Canvas(myImg);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);

        Bitmap img = BitmapFactory.decodeResource(this.getResources(), R.drawable.position).copy(
                Bitmap.Config.ARGB_4444, true);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mCanvas.drawBitmap(Bitmap.createScaledBitmap(img, 40, 40, true),
                curPos.x-20, curPos.y-20, paint);
        mCanvas.drawBitmap(myImg,
                0, 0, mPaint);
//        mCanvas.drawBitmap(Bitmap.createBitmap(myImg, 0, 0, myImg.getWidth(), myImg.getHeight()),
//                0, 0, mPaint);
        mPaint.setAntiAlias(true);
        inner.setImageBitmap(myImg);
        img.recycle();
    }

    public void drawRoute(String exitInfo) {
        if (exitInfo.equals("")) {
            return;
        }
        int flag = -1;
        int y = 0;
        float ratioTop = (float) (topRight.y - topLeft.y) / (topRight.x - topLeft.x);
        float rationBottom = (float) (bottomRight.y - bottomLeft.y) / (bottomRight.x - bottomLeft.x);
        Log.i(TAG, "ratioTop " + ratioTop);
        Log.i(TAG, "rationBottom " + rationBottom);
        ArrayList<Point> points = new ArrayList<>();
        points.add(curPos);
        Point exitPoint = EXIT.get((int) exitInfo.charAt(0) - 65);

        //outOfBound
        if (curPos.x < bottomLeft.x || curPos.y < topLeft.y ||
                curPos.x > topRight.x || curPos.y > bottomRight.y) {
            flag = -1;
            Log.i(TAG, "out of bound");
        } else {
            if (TABLE.containsKey(exitInfo)) {
                ArrayList<Integer> towards = TABLE.get(exitInfo);
                for (Integer i : towards) {
                    Log.i(TAG, "i= " + i);
                    if ((i == 1 || i == 2) && curPos.y > topLeft.y) {
                        y = topLeft.y - (int) ((topRight.x - curPos.x) * ratioTop);
                    } else if ((i == 3 || i == 4) && curPos.y < bottomRight.y) {
                        y = bottomLeft.y + (int) ((curPos.x - bottomLeft.x) * rationBottom);
                    }
                    if (towards.size() == 1) {
                        flag = 1;
                        Log.i(TAG, "flag= " + flag);
                        points.add(new Point(curPos.x, y));
                        points.add(RECT.get(i - 1));
                        points.add(exitPoint);
                        break;
                    } else if (RECT.get(i - 1).x < Math.max(curPos.x, exitPoint.x) &&
                            RECT.get(i - 1).x > Math.min(curPos.x, exitPoint.x) &&
                            RECT.get(i - 1).y < Math.max(curPos.y, exitPoint.y) &&
                            RECT.get(i - 1).y > Math.min(curPos.y, exitPoint.y)) {
                        flag = 2;
                        points.add(new Point(curPos.x, y));
                        points.add(RECT.get(i - 1));
                        points.add(exitPoint);
                        Log.i(TAG, "flag= " + flag);
                        break;
                    }
                }
            } else {
                Toast.makeText(this, "There is no exit " + exitInfo +" in this station", Toast.LENGTH_SHORT);
                return;
            }
        }
            Log.i(TAG, "y= " + y);
            if (flag == -1) {
                Log.i(TAG, "flag= " + flag);
                points.add(new Point(curPos.x, y));
                points.add(exitPoint);
            }

            for (int i = 0; i + 1 < points.size(); i++) {
                mPaint.setColor(Color.RED);
                mPaint.setStrokeWidth(5);
                mCanvas.drawLine(points.get(i).x, points.get(i).y,
                        points.get(i + 1).x, points.get(i + 1).y, mPaint);
                inner.invalidate();
            }
    }

    private void drawMarker(String facility){
        Log.i(TAG, "facility "+facility);
        int index = Character.getNumericValue(facility.charAt(0));
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(3);
        mCanvas.drawCircle(FACILITY.get(index).x, FACILITY.get(index).y, 25, mPaint);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (activity.equals("Display")) {
            Intent intent = new Intent(InnerStationActivity.this, ResultActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (myImg!=null && myImg.isRecycled()) {
            myImg.recycle();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }
}
