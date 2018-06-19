package com.github.rodlibs.mylibgetimagecamera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * Created by rodd on 07/12/17.
 */

public class CamPickerColor implements Camera.PreviewCallback,SurfaceHolder.Callback {

    private Camera mCamera;
    private CameraPreview mPreview;
    private static boolean isFront = false;
    private Context context;
    public ColorListener colorListener;
    private FrameLayout frameLayout;


    public void setListener(ColorListener colorListener) {
        this.colorListener = colorListener;
    }
    public CamPickerColor(Context context,FrameLayout frameLayout) {
        this.context = context;
        this.frameLayout = frameLayout;
    }




    private Point getScreenMetrics(Context context){
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        return new Point(w_screen, h_screen);
    }



    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Point p = getScreenMetrics(context);
        int frameHeight = camera.getParameters().getPreviewSize().height;
        int frameWidth = camera.getParameters().getPreviewSize().width;
        int rgb[] = new int[frameWidth * frameHeight];
        int[] pixels = decodeYUV420SP(rgb, data, frameWidth, frameHeight);

        String hexadecimal = Integer.toHexString(pixels[pixels.length / 2+p.y/2]);
        int r = Integer.valueOf( hexadecimal.substring( 1, 3 ), 16 );
        int g = Integer.valueOf( hexadecimal.substring( 3, 5 ), 16 );
        int b = Integer.parseInt( hexadecimal.substring( 5, 7 ), 16 );

        colorListener.getColor(pixels[pixels.length / 2+p.y/2]);
        colorListener.getColorHexadec("#"+hexadecimal);
        colorListener.getColorRGB(r,g,b);
    }




    private int[] decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;

            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;

                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);
                if (r < 0)
                    r = 0;

                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;

                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;

                else if (b > 262143)
                    b = 262143;
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
        return rgb;
    }








    public void createCamera() {
        try {
                mCamera = getCameraInstance(isFront);
                if (mCamera != null) {
                    mPreview = new CameraPreview(context, mCamera);
                    FrameLayout preview = frameLayout;
                    preview.removeAllViews();
                    preview.addView(mPreview);
                    mPreview.getHolder().addCallback(this);
                }
        } catch (Exception e) {}
    }




    private Camera getCameraInstance(boolean front) {
        Camera c = null;

        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        try {
            if (front) {
                if (Camera.getNumberOfCameras() > 1) {
                    c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    isFront = true;
                } else if (Camera.getNumberOfCameras() > 0) {
                    c = Camera.open(0);
                    isFront = false;
                }
            } else {
                if (Camera.getNumberOfCameras() > 0) {
                    c = Camera.open(0);
                    isFront = false;
                }
            }
            if (c != null) {
                c.setDisplayOrientation(90);
            }
        } catch (Exception e) {}
        return c;
    }



    public void pause(){
        try {
            mCamera.stopPreview();
        }catch (Exception e){}
    }



    public void resume(){
        try {
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
        }catch (Exception e){}
    }



    public void destroyCamera() {
        try {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            this.mCamera.release();
            this.mCamera = null;
        } catch (Exception e) {}
    }



    public void setCameraBack(){
        if(this.mCamera != null) {
            destroyCamera();
        }
        isFront = false;
        createCamera();
    }


    public void setCameraFront(){
        if(this.mCamera != null) {
            destroyCamera();
        }
        isFront = true;
        createCamera();
    }





    @Override
    public void surfaceCreated(SurfaceHolder holder) {}
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();

        } catch (Exception ex) {}
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}
}