package com.senior.arexplorer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.senior.arexplorer.AR.saveObj;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class SaveLocationFragment extends Fragment implements IFragSettings{

    private TextView nameInputTextView;
    private TextView descInputTextView;
    private Switch privateSwitch;
    private Button takePicButton, saveButton;
    private ImageView pictureImageView;
    private Bitmap dbBM;

    private String curPhotoPath;

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent(){
        Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePicIntent.resolveActivity(getActivity().getPackageManager()) != null){

            File photoFile = null;
            try{
                photoFile = createImageFile();
            }
            catch(IOException e){
                System.out.println(e);
            }
            if(photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(getContext(), "com.senior.arexplorer.fileprovider", photoFile);
                takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePicIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imgFileName = "JPEG_"+timeStamp+"_";
        File storeDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File img = File.createTempFile(imgFileName,".jpg", storeDir);

        curPhotoPath = img.getAbsolutePath();
        return img;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.i("onActivityRequest", "requestCode: "+requestCode+"\t resultCode: "+resultCode+"\t Intent: "+data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            //pictureImageView.setImageBitmap(imageBitmap);
            setPic();
        }
    }

    private void setPic() {
        Log.i("setPic", "you're in setPic method!");

        int targetW,targetH,photoW,photoH,scaleFactor;
        //get dimensions of view
        targetW = pictureImageView.getWidth();
        targetH = pictureImageView.getHeight();

        //get dimensions of bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        photoW = bmOptions.outWidth;
        photoH = bmOptions.outHeight;

        //determine scale
        scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        //Rotation matrix
        Matrix m = new Matrix();
        m.postRotate(90);


        Bitmap bm = BitmapFactory.decodeFile(curPhotoPath,bmOptions);
        Log.i("h&w","photoW: "+bm.getWidth()+" \tphotoH: "+bm.getHeight());

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(curPhotoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Log.i("orientation","ori: "+orientation);

        Bitmap rotBM = rotateBitmap(bm,orientation);
        dbBM = rotBM;
        pictureImageView.setImageBitmap(rotBM);

    }


    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_save, container, false);

        nameInputTextView = inflate.findViewById(R.id.nameInput);
        descInputTextView = inflate.findViewById(R.id.saveDescription);
        privateSwitch = inflate.findViewById(R.id.privateSwitch);

        takePicButton = inflate.findViewById(R.id.pictureButton);
        takePicButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //cause camera intent to take picture
                dispatchTakePictureIntent();
                Log.i("button", "you clicked take picture!");
            }
        });
        saveButton = inflate.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.i("button", "you clicked save!");
                saveData();
            }
        });
        pictureImageView = inflate.findViewById(R.id.pictureView);
        return inflate;
    }


    private void saveData() {
        String userID,locName,locDesc,fileName;
        double locLatitude,locLongitude,locElevation;
        Boolean priv;

        userID = "test1";       //TODO

        locLatitude = 0;
        locLongitude = 0;
        locElevation = 0;

        fileName = "save_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        locName = nameInputTextView.getText().toString();
        locDesc = descInputTextView.getText().toString();
        priv = privateSwitch.isChecked();

        saveObj s = new saveObj(userID,locName,locDesc,locLatitude,locLongitude,locElevation,priv);
        s.setBLOB(dbBM);

        try {
            FileOutputStream fos = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(s);
            Log.i("savePath",getContext().getFileStreamPath(fileName).getAbsolutePath());
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("file error","got into file i/o catch");
        }

    }

    @Override
    public void onPause() {
        //save stuff here
        super.onPause();
    }

    @Override
    public void loadSettings(Menu menu, DrawerLayout drawer) {

    }

}
