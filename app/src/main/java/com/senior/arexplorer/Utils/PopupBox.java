package com.senior.arexplorer.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
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
        retView.setTypeface(null, Typeface.BOLD);
        return retView;
    }

    public static TextView getTextView(String textIn, Context context){
        TextView textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        textView.setPadding(10,5,10,5);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(18);
        textView.setText(textIn);
        return textView;
    }

}
