package com.galiazat.videoStreamingCmt.streaming.source;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

import com.galiazat.videoStreamingCmt.custom.VideoSourcePreviewView;
import com.galiazat.videoStreamingCmt.streaming.codecs.VideoEncoderThread;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Azat Galiullin.
 */

public class ServerSocketThread extends Thread implements VideoEncoderThread.VideoCoderListener {

    private int port;
    private List<InternalServerSocketThread> internalThreads = new ArrayList<>();
    private ServerSocket ss;
    private VideoEncoderThread videoEncoderThread;

    public ServerSocketThread(int port) {
        this.port = port;
        videoEncoderThread = new VideoEncoderThread(this);
    }

    @Override
    public void run() {
        try {
            videoEncoderThread.prepare();
            videoEncoderThread.start();
            ss = new ServerSocket(port);
            while (!isInterrupted()){
                try {
                    Socket s = ss.accept();
                    InternalServerSocketThread thread = new InternalServerSocketThread(s);
                    internalThreads.add(thread);
                    thread.start();
                } catch (SocketException s){
                    s.printStackTrace();
                }
            }
        }catch(Exception e){
            Log.d("ServerThread", "run: erro");
        }
    }

    @Override
    public void interrupt() {
        videoEncoderThread.release();
        for (InternalServerSocketThread serverSocketThread : internalThreads){
            serverSocketThread.interrupt();
        }
        if (ss != null){
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.interrupt();
    }

    public void sendFrame(byte[] frame){
        videoEncoderThread.setFrameData(frame);
    }

    @Override
    public void frameCoded(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[byteBuffer.limit()];
        byteBuffer.get(bytes, 0, byteBuffer.limit());
        VideoSourcePreviewView.SendingFrame sendingFrame =
                new VideoSourcePreviewView.SendingFrame(bytes,
                        1, 720, 1280);
        for (InternalServerSocketThread serverSocketThread : internalThreads){
            serverSocketThread.sendFrame(sendingFrame);
        }
    }

    private class InternalServerSocketThread extends Thread{
        private Socket s = null;
        private OutputStream os = null;
        volatile private VideoSourcePreviewView.SendingFrame sendingFrame;
        volatile private ByteArrayOutputStream stream;
        final private Object blockObject = new Object();

        InternalServerSocketThread(Socket s) {
            this.s = s;
        }

        void sendFrame(VideoSourcePreviewView.SendingFrame sendingFrame){
            if (sendingFrame != null){
                synchronized (blockObject) {
                    this.sendingFrame = sendingFrame;
                    YuvImage yuvimage=new YuvImage(sendingFrame.getFrame(), ImageFormat.NV21,
                            sendingFrame.getFrameWidth(),
                            sendingFrame.getFrameHeight(),null);
                    stream = new ByteArrayOutputStream();
                    yuvimage.compressToJpeg(new Rect(0,0,sendingFrame.getFrameWidth(),
                            sendingFrame.getFrameHeight()),80,stream);
                    blockObject.notify();
                }
            }
        }

        @Override
        public void interrupt() {
            super.interrupt();
            synchronized (blockObject) {
                blockObject.notify();
            }
        }

        @Override
        public void run() {
            if(s !=null){
                String clientIp = s.getInetAddress().toString().replace("/", "");
                int clientPort = s.getPort();
                Log.d("ip","====client ip====="+clientIp);
                System.out.println("====client port====="+clientPort);
                try {
                    s.setKeepAlive(true);
                    os = s.getOutputStream();
                    while(!isInterrupted()){
                        VideoSourcePreviewView.SendingFrame copy;
                        ByteArrayOutputStream baos;
                        synchronized (blockObject) {
                            copy = sendingFrame;
                            baos = stream;
                        }
                        if (copy != null){
                            DataOutputStream dos = new DataOutputStream(os);
                            dos.writeInt(sendingFrame.getPreviewType());
                            dos.writeInt(sendingFrame.getFrameHeight());
                            dos.writeInt(sendingFrame.getFrameWidth());

                            byte[] bytes = baos.toByteArray();
                            int size = bytes.length;
                            dos.writeInt(size);
                            dos.flush();
                            dos.write(bytes);
                            //System.out.println("outlength"+mPreview.mFrameBuffer.length);
                            dos.flush();
                        } else {
                            try {
                                synchronized (blockObject) {
                                    blockObject.wait();
                                }
                            } catch (InterruptedException e){
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (os!= null)
                            os.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
            else{
                System.out.println("socket is null");
            }
        }
    }
}
