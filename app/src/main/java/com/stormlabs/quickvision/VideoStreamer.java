package com.stormlabs.quickvision;


import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.Deflater;

class VideoStreamer extends Thread {

    private boolean isStopped;
    private Mat lastFrame = null;

    private InetAddress serverAddress;
    private int serverPort;
    private DatagramSocket udpSocket;


    VideoStreamer(String serverIP, int serverPort, int localPort) throws SocketException, UnknownHostException {
        udpSocket = new DatagramSocket(localPort);
        serverAddress = InetAddress.getByName(serverIP);
        this.serverPort = serverPort;
    }


    void setLastFrame(Mat f){
        this.lastFrame = f;
    }


    public void run() {

        this.isStopped = false;

        while (!isStopped) {
            if (lastFrame != null) {
                try {
                    long t1 = System.currentTimeMillis();
                    sendLastFrame();
                    Thread.sleep(40);
                    long timeToSend = System.currentTimeMillis() - t1;
                    Log.d(MainActivity.TAG,"NFPS: " + 1000 / timeToSend);
                } catch (IOException|InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        udpSocket.close();
    }


    private void sendLastFrame() throws IOException{

        // Creating bitmap and compressing to JPEG with low quality
        Bitmap tmpBitmap = Bitmap.createBitmap(lastFrame.cols(), lastFrame.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(lastFrame, tmpBitmap);

        ByteArrayOutputStream tmpStream = new ByteArrayOutputStream();
        tmpBitmap.compress(Bitmap.CompressFormat.JPEG, 10, tmpStream);
        byte[] jpgByteArray = tmpStream.toByteArray();
        tmpStream.close();

        // Compressing with zlib
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
        long timeStamp = System.currentTimeMillis();

        // Getting the number of packets needed to send the data
        int chunkSize = 480;
        int packsNum = compressedBytes.length / chunkSize;
        if( compressedBytes.length % chunkSize > 0 ) packsNum += 1;

        // Sending the packets
        int base = 0;
        for (int i = 1; i <= packsNum; i++) {
            ByteBuffer buff = ByteBuffer.allocate(chunkSize + Long.SIZE / Byte.SIZE + 2 * Integer.SIZE / Byte.SIZE);
            buff.putLong(timeStamp);
            buff.putInt(i);
            buff.putInt(packsNum);
            buff.put(Arrays.copyOfRange(compressedBytes, base, base + chunkSize));
            DatagramPacket packet = new DatagramPacket(buff.array(), buff.array().length, serverAddress, serverPort);
            udpSocket.send(packet);
            base += chunkSize;
        }

        Log.d(MainActivity.TAG, "frame sent with length: " + compressedBytes.length );

    }


    void stopStreaming(){
        this.isStopped = true;
        this.lastFrame = null;
    }


}
