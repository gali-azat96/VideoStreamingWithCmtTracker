package com.galiazat.diplomtest4opencvimplement.streaming.source;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

import com.galiazat.diplomtest4opencvimplement.custom.VideoSourcePreviewView;

import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Azat Galiullin.
 */

public class ServerSocketThread extends Thread {

    private int port;
    private List<InternalServerSocketThread> internalThreads = new ArrayList<>();
    private ServerSocket ss;

    public ServerSocketThread(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
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

    public void sendFrame(VideoSourcePreviewView.VideoSourceListener.SendingFrame sendingFrame){
        for (InternalServerSocketThread serverSocketThread : internalThreads){
            serverSocketThread.sendFrame(sendingFrame);
        }
    }

    @Override
    public void interrupt() {
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

    private class InternalServerSocketThread extends Thread{
        private Socket s = null;
        private OutputStream os = null;
        volatile private VideoSourcePreviewView.VideoSourceListener.SendingFrame sendingFrame;
        volatile private ByteArrayOutputStream stream;
        final private Object blockObject = new Object();

        InternalServerSocketThread(Socket s) {
            this.s = s;
        }

        void sendFrame(VideoSourcePreviewView.VideoSourceListener.SendingFrame sendingFrame){
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
                        VideoSourcePreviewView.VideoSourceListener.SendingFrame copy;
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
