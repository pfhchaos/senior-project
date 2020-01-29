package com.senior.arexplorer.Utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class PopupBox extends AlertDialog {

    public PopupBox(@NonNull Context context, String title) {
        super(context);
        setCustomTitle(getTitleView(title));
    }

    private TextView getTitleView(String title){
        TextView retView = new TextView(getContext());
        retView.setText(title);
        retView.setPadding(10, 10, 10, 10);
        retView.setGravity(Gravity.CENTER);
        retView.setTextSize(20);
        return retView;
    }

}
