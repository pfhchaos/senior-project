package com.senior.arexplorer.AR;

import android.app.AlertDialog;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.senior.arexplorer.R;
import com.senior.arexplorer.IFragSettings;
import com.senior.arexplorer.SeekBarWithText;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

public class ARFragment extends Fragment implements IFragSettings {

    private CameraOverlay mOverlay;
    private TextureView camView;
    private final int FOV_MIN = 45, FOV_MAX = 180; //in degrees
    private final int DD_MIN = 100, DD_MAX = 10000; //in meters
    private int fov = 90, drawDistance = 1000;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ar, container, false);

        camView = view.findViewById(R.id.camera_view);
        camView.post(this::startCamera);

        mOverlay = new CameraOverlay(view.getContext());
        FrameLayout overlay = view.findViewById(R.id.overlay_view);
        overlay.addView(mOverlay);
        mOverlay.toggleTimer();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mOverlay.kill();
    }

    @Override
    public void onPause(){
        super.onPause();
        mOverlay.toggleTimer();
    }

    @Override
    public void onResume(){
        super.onResume();
        mOverlay.toggleTimer();


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

    public void loadSettings(Menu menu, DrawerLayout drawer){
        menu.removeGroup(R.id.settings);
        TextView title = new TextView(drawer.getContext());
        title.setText("");
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(20);

        MenuItem fovItem = menu.add(R.id.settings, Menu.NONE, Menu.NONE, "Compass Field of View : " + fov + " degrees");
        fovItem.setOnMenuItemClickListener((i) -> {
            AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
            title.setText("Please Select a Field of View");
            popDialog.setCustomTitle(title);

            SeekBarWithText popView = new SeekBarWithText(getContext());
            popView.setSeekbarMinMaxStep(FOV_MIN, FOV_MAX, 1)
                    .setSeekbarProgress(fov - FOV_MIN)
                    .setSeekbarText("Current Field of View : " + fov)
                    .setSeekbarListener(new SeekBar.OnSeekBarChangeListener(){
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                            fov = progress + FOV_MIN;
                            popView.setSeekbarText("Current Field of View : " + fov);
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {}

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {}
                    });

            popDialog.setPositiveButton("OK", (dialog, which) -> {
                fovItem.setTitle("Compass Field of View : " + fov + " degrees");
                mOverlay.setFoV(fov);
                drawer.closeDrawer(GravityCompat.START);
                dialog.dismiss();
            });

            popDialog.setView(popView);
            popDialog.create();
            popDialog.show();
            return true;
        });

        MenuItem ddItem = menu.add(R.id.settings, Menu.NONE, Menu.NONE, "Draw Distance : " +  drawDistance+ " meters");
        ddItem.setOnMenuItemClickListener((i) -> {
            AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
            title.setText("Please Select a Draw Distance");
            popDialog.setCustomTitle(title);

            SeekBarWithText popView = new SeekBarWithText(getContext());
            popView.setSeekbarMinMaxStep(DD_MIN, DD_MAX, 100)
                   .setSeekbarProgress(drawDistance - DD_MIN)
                   .setSeekbarText("Current Draw Distance : " + drawDistance)
                   .setSeekbarListener(new SeekBar.OnSeekBarChangeListener(){
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                            fov = progress + FOV_MIN;
                            popView.setSeekbarText("Current Field of View : " + fov);
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {}

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {}
                   });

            popDialog.setPositiveButton("OK", (dialog, which) -> {
                ddItem.setTitle("Draw Distance : " + drawDistance + " meters");
                mOverlay.setDD(drawDistance);
                drawer.closeDrawer(GravityCompat.START);
                dialog.dismiss();
            });

            popDialog.setView(popView);
            popDialog.create();
            popDialog.show();
            return true;
        });
    }
}
