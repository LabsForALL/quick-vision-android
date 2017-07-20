package com.stormlabs.quickvision;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends Activity
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    static{
        System.loadLibrary("opencv_java3");
    }

    private static final String  TAG = "QuickVision";
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 99;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat tmpMat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting up the camera view
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.image_manipulations_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView.setCvCameraViewListener(this);

        // Checking the camera permission and waiting for callback
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Starting processing
                    mOpenCvCameraView.enableView();
                } else {
                    // Closing activity
                    this.finish();
                }
                break;
            }
        }
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        tmpMat = new Mat();
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat rgba = inputFrame.rgba();

        Imgproc.cvtColor(tmpMat, rgba, Imgproc.COLOR_GRAY2BGRA, 4);
        Imgproc.Canny(rgba, tmpMat, 80, 100);

        /*
        int length = (int) (rgba.total() * rgba.elemSize());
        byte buffer[] = new byte[length];
        rgba.get(0, 0, buffer);
        */

        Bitmap tmpBitmap = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba, tmpBitmap);


        return rgba;
    }


    @Override
    public void onCameraViewStopped() {
        if (tmpMat != null)
            tmpMat.release();
        tmpMat = null;
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();
    }


    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();
    }

}