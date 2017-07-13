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
import java.util.Arrays;
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

                // Getting the number of packets needed to send the data
                int packsNum = dataToSend.length / 256;
                if( dataToSend.length % 256 > 0 ) packsNum += 1;

                int base = 0;
                for (int i = 0; i < packsNum; i++) {
                    ByteBuffer buff = ByteBuffer.allocate(256 + timeStamp.length + 2 * Integer.SIZE / Byte.SIZE);
                    buff.put(timeStamp);
                    buff.putInt(i);
                    buff.putInt(packsNum);
                    buff.put(Arrays.copyOfRange(dataToSend, base, base + 256));
                    DatagramPacket packet = new DatagramPacket(buff.array(), buff.array().length, serverAddr, serverPort);
                    udpSocket.send(packet);
                    Log.d(TAG, "sent " + packet.getLength() + " bytes");
                    base += 256;
                }

            } catch (SocketException e) {
                Log.e("Udp:", "Socket Error:", e);
            } catch (IOException e) {
                Log.e("Udp Send:", "IO Error:", e);
            }
        }
    }
}
