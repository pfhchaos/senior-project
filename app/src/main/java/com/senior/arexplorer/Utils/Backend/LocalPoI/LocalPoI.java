package com.senior.arexplorer.Utils.Backend.LocalPoI;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.senior.arexplorer.Utils.Backend.PoI;
import com.senior.arexplorer.Utils.Backend.saveObj;
import com.senior.arexplorer.Utils.PopupBox;


public class LocalPoI extends PoI {

    Drawable img = null;

    public LocalPoI(saveObj s) {
        super();
        setName(s.getLocationName());
        setDescription(s.getLocationDesc());
        setElevation(s.getLocationElevation());
        setLatitude(s.getLocationLatitude());
        setLongitude(s.getLocationLongitude());
        if(s.getBlob() != null) {
            this.img = new BitmapDrawable(blobToBitmap(s.getBlob()));
            Log.v("LocalPoI","getBlob was not null");
        }
    }

    private Bitmap blobToBitmap(byte[] blobToConvert){
        if(blobToConvert == null) return null;
        Bitmap bitMapImg = BitmapFactory.decodeByteArray(blobToConvert,0,blobToConvert.length);
        return bitMapImg;
    }

    @Override
    public boolean onLongTouch(Context context) {
        PopupBox popup = new PopupBox(context, getName());
        popup.setView(getDetailsView(context));
        popup.show();
        return true;
    }

    @Override
    public View getDetailsView(Context context){
        TextView retView = new TextView(context);
        retView.setPadding(10,5,10,5);
        retView.setGravity(Gravity.CENTER);
        retView.setText(toShortString());

        int h,w;
        h=img.getIntrinsicHeight()*5;
        w=img.getIntrinsicWidth()*5;

        img.setBounds(0,0,w,h);
        retView.setCompoundDrawables(null,null,null,img);
        retView.setTextSize(18);
        return retView;
    }
}
