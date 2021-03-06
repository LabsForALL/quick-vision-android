package com.stormlabs.quickvision;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.net.SocketException;
import java.net.UnknownHostException;

public class VideoActivity extends Activity
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    static{
        System.loadLibrary("opencv_java3");
    }

    public static final String  TAG = "QuickVision";
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 99;
    private CameraBridgeViewBase mOpenCvCameraView;
    private VideoStreamer videoStreamer;


    /* Activity lifecycle methods */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        // getting info
        Intent intent = getIntent();
        String ip = intent.getStringExtra(MainActivity.IP);
        String port = intent.getStringExtra(MainActivity.PORT);

        Log.d(TAG, ip + " " + port);

        // Setting up the camera view
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.image_manipulations_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

    }


    @Override
    protected void onResume() {
        super.onResume();

        // Checking the camera permission and waiting for callback
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            mOpenCvCameraView.enableView();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();
    }


    /* Camera lifecycle methods */

    @Override
    public void onCameraViewStarted(int width, int height) {

        // Setting up the video streamer
        try {
            videoStreamer = new VideoStreamer("192.168.0.20", 10000, 6000);
            videoStreamer.start();
        }catch (SocketException|UnknownHostException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Network error occurred", Toast.LENGTH_SHORT).show();
            finish();
        }

    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // Getting the frame as mat
        Mat rgba = inputFrame.rgba();
        videoStreamer.setLastFrame(rgba);
        return rgba;
    }


    @Override
    public void onCameraViewStopped() {
        videoStreamer.stopStreaming();
    }

}