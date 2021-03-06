package com.senior.arexplorer.AR;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.util.Supplier;
import androidx.drawerlayout.widget.DrawerLayout;

import com.senior.arexplorer.R;
import com.senior.arexplorer.Utils.Backend.Here.Here;
import com.senior.arexplorer.Utils.CompassAssistant;
import com.senior.arexplorer.Utils.FragmentWithSettings;
import com.senior.arexplorer.Utils.SeekBarWithText;
import com.senior.arexplorer.Utils.Settings;

public class ARFragment extends FragmentWithSettings {

    private CameraOverlay mOverlay;
    private TextureView camView;
    private final int FOV_MIN = 45, FOV_MAX = 360; //in degrees
    private final int DD_MIN = 100, DD_MAX = 10000; //in meters

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ar, container, false);

        camView = view.findViewById(R.id.camera_view);
        camView.post(this::startCamera);

        mOverlay = new CameraOverlay(view.getContext());
        FrameLayout overlay = view.findViewById(R.id.overlay_view);
        overlay.addView(mOverlay);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Here.getInstance().removeListener(mOverlay);
        //this does not seem like the correct solution to the current crash
        CompassAssistant compassAssistant = CompassAssistant.getInstance();
        if (compassAssistant != null) {
            compassAssistant.removeCompassListener(mOverlay);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    private void startCamera(){
        PreviewConfig config = new PreviewConfig.Builder().build();
        Preview preview = new Preview(config);


        preview.setOnPreviewOutputUpdateListener(
                previewOutput -> {
                    ViewGroup parent = (ViewGroup) camView.getParent();
                    parent.removeView(camView);
                    parent.addView(camView, 0);

                    camView.setSurfaceTexture(previewOutput.getSurfaceTexture());

                    updateTransform();
                });


        CameraX.bindToLifecycle(this, preview);
    }

    private void updateTransform(){
        Matrix mx = new Matrix();
        float w = camView.getMeasuredWidth();
        float h = camView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int)camView.getRotation();

        switch(rotation){
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float)rotationDgr, cX, cY);
        camView.setTransform(mx);
    }

    public void loadSettingsUI(Menu menu, DrawerLayout drawer, Context context){

        menu.removeGroup(R.id.settings);
        Function<String, TextView> getTitle = (i) -> {
          TextView title = new TextView(drawer.getContext());
          title.setText(i);
          title.setPadding(10, 10, 10, 10);
          title.setGravity(Gravity.CENTER);
          title.setTextSize(20);
          return title;
        };

        int fov  = Settings.getInstance().getCompassFOV();

        menu.add(R.id.settings, Menu.NONE, Menu.NONE, "Compass Field of View : " + fov + " degrees")
            .setOnMenuItemClickListener((i) -> {
                AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
                popDialog.setCustomTitle(getTitle.apply("Please Select a Compass Field of View"));

                SeekBarWithText popView = new SeekBarWithText(getContext());
                popView.setMinMax(FOV_MIN, FOV_MAX)
                        .setProgress(Settings.getInstance().getCompassFOV() - FOV_MIN)
                        .setText("Current Field of View : " + Settings.getInstance().getCompassFOV())
                        .setListener((progress) -> {
                                Settings.getInstance().setCompassFOV(progress + FOV_MIN);
                                popView.setText("Current Field of View : " + Settings.getInstance().getCompassFOV());
                        });

                popDialog.setPositiveButton("OK", (dialog, which) -> {
                    i.setTitle("Compass Field of View : " + Settings.getInstance().getCompassFOV() + " degrees");
                    dialog.dismiss();
                });

                popDialog.setView(popView);
                popDialog.show();
                return false;
            });

        Supplier<String> formatDistance = () -> {
            int drawDistance = Settings.getInstance().getDrawDistance();
            return ((drawDistance >= 1000) ? ((float) drawDistance / 1000) + " km" : drawDistance + " meters");
        };

        menu.add(R.id.settings, Menu.NONE, Menu.NONE, "Draw Distance : " + formatDistance.get() )
            .setOnMenuItemClickListener((i) -> {
                int drawDistance = Settings.getInstance().getDrawDistance();
                AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
                popDialog.setCustomTitle(getTitle.apply("Please Select a Draw Distance"));

                /* Note the 100s here are to get the bar to move in increments of 100
                 * because fucking android is fucking stupid and doesnt allow you to set the fucking step size
                 * of the fucking seekbar you have to define a set amount of steps
                 * and fucking scale it when storing because fuck android*/
                SeekBarWithText popView = new SeekBarWithText(getContext());
                popView.setMinMax(DD_MIN / 100, DD_MAX / 100)
                        .setProgress((drawDistance - DD_MIN) / 100)
                        .setText("Current Draw Distance : " + formatDistance.get())
                        .setListener((progress) -> {
                            Settings.getInstance().setDrawDistance((progress * 100) + DD_MIN);
                            popView.setText("Current Draw Distance : " + formatDistance.get());
                        });

                popDialog.setPositiveButton("OK", (dialog, which) -> {
                    i.setTitle("Draw Distance : " + formatDistance.get());
                    dialog.dismiss();
                });
                popDialog.setOnCancelListener((dialog) -> {
                    Settings.getInstance().setDrawDistance(drawDistance);
                    i.setTitle("Draw Distance : " + drawDistance);
                    dialog.dismiss();
                });

            popDialog.setView(popView);
            popDialog.create();
            popDialog.show();
            return false;
        });
    }
}
