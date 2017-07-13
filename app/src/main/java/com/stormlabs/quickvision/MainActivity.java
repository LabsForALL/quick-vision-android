package com.stormlabs.quickvision;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;

public class MainActivity extends AppCompatActivity {

    final static String TAG = "QuickVision";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendImage();
            }
        });

    }

    private void sendImage(){

        // Getting the photo bitmap
        InputStream imgStream = getResources().openRawResource(getResources().getIdentifier(
                "img1",
                "raw",
                getPackageName()));
        Bitmap bitmap = BitmapFactory.decodeStream(imgStream);

        // Compressing to JPEG with low quality
        ByteArrayOutputStream tmpStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, tmpStream);
        byte[] jpgByteArray = tmpStream.toByteArray();

        // Closing streams
        try {
            tmpStream.close();
            imgStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Compressing with zlib
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(jpgByteArray);
        deflater.finish();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        while (!deflater.finished()) {
            int byteCount = deflater.deflate(buf);
            baos.write(buf, 0, byteCount);
        }
        deflater.end();
        byte[] compressedBytes = baos.toByteArray();

        // Sending via UDP
        (new Thread(new ClientSend(compressedBytes))).start();

    }


    public class ClientSend implements Runnable {

        byte[] dataToSend = null;
        final String serverIP = "192.168.0.5";
        final int serverPort = 10000;

        ClientSend(byte[] data){
            this.dataToSend = data;
        }

        @Override
        public void run() {
            try {

                DatagramSocket udpSocket = new DatagramSocket(6000);

                InetAddress serverAddr = InetAddress.getByName(serverIP);
                byte[] buf = ("The String to Send").getBytes();

                byte[] timeStamp = ByteBuffer.allocate(Long.SIZE / Byte.SIZE)
                        .putLong(System.currentTimeMillis()).array();

                // Calculating packets
                int packetsNum = this.dataToSend.length/256 + 1;
                for (int i = 0; i < packetsNum; i++) {





                }




                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, serverPort);
                for (int i = 0 ; i < 10; i++){
                    udpSocket.send(packet);
                    Log.d(TAG,"Packet sent");
                }

            } catch (SocketException e) {
                Log.e("Udp:", "Socket Error:", e);
            } catch (IOException e) {
                Log.e("Udp Send:", "IO Error:", e);
            }
        }
    }
}
