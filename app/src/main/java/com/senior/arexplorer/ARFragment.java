package com.senior.arexplorer;

import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ARFragment extends Fragment {
    private Camera mCamera;
    private CameraView mView;
    private CameraOverlay mOverlay;
    private FrameLayout preview;
    private FrameLayout overlay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ar, container, false);
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);

        mView = new CameraView(view.getContext(), mCamera);
        preview = view.findViewById(R.id.camera_view);
        preview.addView(mView);

        mOverlay = new CameraOverlay(view.getContext());
        overlay = view.findViewById(R.id.overlay_view);
        overlay.addView(mOverlay);
        mOverlay.toggleTimer();

        return view;
    }

    private static Camera getCameraInstance(){
        Camera cam = null;
        try{
            cam = Camera.open();
        } catch (Exception e) {
            Log.d(TAG, "Camera not available. In use or does not exist?");
        }
        return cam;
    }

    @Override
    public void onResume() {
        super.onResume();

        mCamera = getCameraInstance();
        //mCamera.setDisplayOrientation(90);
        mOverlay.toggleTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCam();
    }

    @Override
    public void onStop() {
        super.onStop();
        releaseCam();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        releaseCam();
    }


    public void releaseCam(){
        if (mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
        }
        mOverlay.stopTimer();
    }
}
