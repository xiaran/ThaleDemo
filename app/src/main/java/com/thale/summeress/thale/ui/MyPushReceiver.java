package com.thale.summeress.thale.ui;

import android.content.Context;
import android.widget.Toast;

import com.baidu.android.pushservice.PushMessageReceiver;

import java.util.List;

/**
 * Created by bigstone on 19/9/2017.
 */

public class MyPushReceiver extends PushMessageReceiver {
    @Override
    public void onBind(Context context, int i, String s, String s1, String s2, String s3) {
        Toast.makeText(context, "onBind"+ s, Toast.LENGTH_SHORT).show();
        System.out.println("onBind"+ s);
    }

    @Override
    public void onUnbind(Context context, int i, String s) {
        Toast.makeText(context, "onUnbind"+ s, Toast.LENGTH_SHORT).show();
        System.out.println("onUnbind"+ s);

    }

    @Override
    public void onSetTags(Context context, int i, List<String> list, List<String> list1, String s) {
        Toast.makeText(context, "onSetTags"+ s, Toast.LENGTH_SHORT).show();
        System.out.println("onSetTags"+ s);
    }

    @Override
    public void onDelTags(Context context, int i, List<String> list, List<String> list1, String s) {
        Toast.makeText(context, "onDelTags"+ s, Toast.LENGTH_SHORT).show();
        System.out.println("onDelTags"+ s);
    }

    @Override
    public void onListTags(Context context, int i, List<String> list, String s) {
        Toast.makeText(context, "onListTags"+ s, Toast.LENGTH_SHORT).show();
        System.out.println("onListTags"+ s);
    }

    @Override
    public void onMessage(Context context, String s, String s1) {
        Toast.makeText(context, "onMessage"+ s + ":" + s1, Toast.LENGTH_SHORT).show();
        System.out.println("onListTags"+ s);

    }

    @Override
    public void onNotificationClicked(Context context, String s, String s1, String s2) {
        Toast.makeText(context, "onNotificationClicked"+ s + ":" + s1, Toast.LENGTH_SHORT).show();
        System.out.println("onNotificationClicked"+ s + ":" + s1);
    }

    @Override
    public void onNotificationArrived(Context context, String s, String s1, String s2) {
        Toast.makeText(context, "onNotificationArrived"+ s + ":" + s1, Toast.LENGTH_SHORT).show();
        System.out.println("onNotificationArrived"+ s + ":" + s1);

    }
}
