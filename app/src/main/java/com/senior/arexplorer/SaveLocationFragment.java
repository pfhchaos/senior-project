package com.senior.arexplorer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class SaveLocationFragment extends Fragment {

    private TextView nameInputTextView;
    private TextView descInputTextView;
    private Switch privateSwitch;
    private Button takePicButton, saveButton;
    private ImageView pictureImageView;

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

                //SOMETHING'S WRONG HERE
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
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null){
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            if(imageBitmap != null){
                pictureImageView.setImageBitmap(imageBitmap);
            }
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
                System.out.println("Clicked picture button");
                //cause camera intent to take picture
                dispatchTakePictureIntent();
            }
        });
        saveButton = inflate.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                System.out.println("Clicked save button");
                saveData();
            }
        });
        pictureImageView = inflate.findViewById(R.id.pictureView);
        return inflate;
    }


    private void saveData() {
        String name,desc;
        Boolean priv;


        name = nameInputTextView.getText().toString();
        desc = descInputTextView.getText().toString();
        priv = privateSwitch.isChecked();
    }

    @Override
    public void onPause() {
        //save stuff here
        super.onPause();
    }
}
