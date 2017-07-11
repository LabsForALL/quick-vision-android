package com.stormlabs.quickvision;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final String TAG = "QuickVision";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Getting the photo byte array
        InputStream stream = getResources().openRawResource(getResources().getIdentifier(
                "img1",
                "raw",
                getPackageName()));

        Bitmap bitmap = BitmapFactory.decodeStream(stream);

        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Integer> indexList = new ArrayList<Integer>();

        int bmHeight = bitmap.getHeight();
        int bmWidth = bitmap.getWidth();
        int index = 0;

        for (int i = 0; i < bmHeight; i++) {
            for (int j = 0 ; j < bitmap.getWidth(); j++) {
                if(bitmap.getPixel(i,j) != -1) {
                    indexList.add(index);
                }
                index += 1;
            }
        }


        for (int i = 0; i < indexList.size(); i++) {



        }

    }


    private byte[] streamToByteArray(InputStream stream){

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] result = null;

        try {

            int nRead;
            byte[] data = new byte[4096];

            while ((nRead = stream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            result = buffer.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
