package com.senior.arexplorer;

import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

public class ARFragment extends Fragment {

    private CameraOverlay mOverlay;
    private TextureView camView;
    private FrameLayout overlay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ar, container, false);

        camView = view.findViewById(R.id.camera_view);
        camView.post(this::startCamera);

        mOverlay = new CameraOverlay(view.getContext());
        overlay = view.findViewById(R.id.overlay_view);
        overlay.addView(mOverlay);
        mOverlay.toggleTimer();

        return view;
    }

    void startCamera(){
        PreviewConfig config = new PreviewConfig.Builder().build();
        Preview preview = new Preview(config);


        preview.setOnPreviewOutputUpdateListener(
                previewOutput -> {
                    ViewGroup parent = (ViewGroup) camView.getParent();
                    parent.removeView(camView);
                    parent.addView(camView, 0);

                    camView.setSurfaceTexture(previewOutput.getSurfaceTexture());

                    //updateTransform();
                });


        CameraX.bindToLifecycle((LifecycleOwner) this, preview);
    }

    private void updateTransform(){
        Matrix mx = new Matrix();
        float w = camView.getMeasuredWidth();
        float h = camView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int)camView.getRotation();
        System.out.println(rotation);

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

        mx.postRotate((float)rotationDgr + 180, cX, cY);
        camView.setTransform(mx);
    }
}
