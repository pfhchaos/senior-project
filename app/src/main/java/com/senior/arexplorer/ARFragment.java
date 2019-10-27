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
    private FrameLayout preview;
    private FrameLayout drawView;
    private CameraView.CameraOverlay mOverlay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ar, container, false);
        mCamera = getCameraInstance();
        mView = new CameraView(view.getContext(), mCamera);
        preview = view.findViewById(R.id.camera_view);
        preview.addView(mView);
        mCamera.setDisplayOrientation(90);

//        mOverlay = mView.getOverlay(view.getContext());
//        drawView = view.findViewById(R.id.drawing_view);
//        drawView.addView(mOverlay);

        return view;
    }

    public static Camera getCameraInstance(){
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
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mCamera != null)
        {
            mCamera.stopPreview();
            mCamera.release();
        }
    }
}
