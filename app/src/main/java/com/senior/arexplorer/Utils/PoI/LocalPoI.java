package com.senior.arexplorer.Utils.PoI;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.senior.arexplorer.AR.saveObj;
import com.senior.arexplorer.R;
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
    View getDetailsView(Context context){
        TextView retView = new TextView(context);
        retView.setPadding(10,5,10,5);
        retView.setGravity(Gravity.CENTER);
        retView.setText(toShortString());
        retView.setCompoundDrawablesWithIntrinsicBounds(null,img,null,null);
        retView.setTextSize(18);
        return retView;
    }
}
