package com.galiazat.diplomtest4opencvimplement.streaming.source;

import android.util.Log;

import org.opencv.core.Mat;

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

    public void sendMat(Mat mat){
        for (InternalServerSocketThread serverSocketThread : internalThreads){
            serverSocketThread.sendMat(mat);
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
        volatile private Mat sendingMat = null;
        final private Object blockObject = new Object();

        InternalServerSocketThread(Socket s) {
            this.s = s;
        }

        void sendMat(Mat mat){
            sendingMat = mat;
            if (sendingMat != null){
                synchronized (blockObject) {
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
                System.out.println("====client ip====="+clientIp);
                System.out.println("====client port====="+clientPort);
                try {
                    s.setKeepAlive(true);
                    os = s.getOutputStream();
                    while(!isInterrupted()){
                        Mat copy = sendingMat;
                        if (copy != null){
                            int channels = copy.channels();
                            DataOutputStream dos = new DataOutputStream(os);
                            dos.writeInt(copy.type());
                            dos.writeInt(copy.rows());
                            dos.writeInt(copy.cols());
                            dos.writeInt(channels);
                            dos.flush();
                            System.out.println(copy.height() + "x" + copy.width()
                                    + "x" + channels);
                            byte[] matsByte = new byte[copy.height() * copy.width() * channels];
                            copy.get(0,0, matsByte);
                            dos.write(matsByte);
                            //System.out.println("outlength"+mPreview.mFrameBuffer.length);
                            dos.flush();
                        } else {
                            try {
                                blockObject.wait();
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
