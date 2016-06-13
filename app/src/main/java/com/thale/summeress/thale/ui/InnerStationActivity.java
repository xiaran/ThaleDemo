package com.thale.summeress.thale.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.multidex.MultiDex;
import android.os.Bundle;
import android.util.Log;

import com.thale.summeress.thale.R;
import com.thale.summeress.thale.tools.ScaleImageView;

public class InnerStationActivity extends Activity {

    private ScaleImageView inner;
    private String TAG = "innerStationActivity";

    private Canvas mCanvas;
    private Paint mPaint;

    private float curX, curY;

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
        curX = 1015;
        curY = 370;
        setImageAndPos();
    }

    private void setImageAndPos(){
        Bitmap myImg = BitmapFactory.decodeResource(this.getResources(), R.drawable.inner_admiralty).copy(
                Bitmap.Config.ARGB_8888, true);
        Matrix matrix = new Matrix();
        Bitmap map = Bitmap.createBitmap(myImg, 0, 0, myImg.getWidth(), myImg.getHeight(),
                matrix, true);
        Log.i("MyImg", myImg.getWidth()+","+myImg.getHeight());


        mCanvas = new Canvas(map);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        Bitmap img = BitmapFactory.decodeResource(this.getResources(), R.drawable.position).copy(
                Bitmap.Config.ARGB_8888, true);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mCanvas.drawBitmap(Bitmap.createScaledBitmap(img, 40, 40, true),
                curX, curY-30, paint);

        mCanvas.drawBitmap(map, 0, 0, mPaint);
        //canvas.rotate(CONVERT_DEGREE);
        mPaint.setAntiAlias(true);

        inner.setImageBitmap(map);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }
}
