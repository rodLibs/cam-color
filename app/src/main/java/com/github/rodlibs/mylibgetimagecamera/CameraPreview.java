package com.github.rodlibs.mylibgetimagecamera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


@SuppressLint("ViewConstructor")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera) {
        this.mCamera = camera;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.stopPreview();
        }
        catch (Exception e) {}
        try{
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {}
    }

    public void surfaceDestroyed(SurfaceHolder holder) {}

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
}
