package com.galiazat.diplomtest4opencvimplement.streaming.client;

import android.os.Handler;
import android.support.annotation.Nullable;

import org.opencv.core.Mat;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * @author Azat Galiullin.
 */

public class ClientSocketThread extends Thread {
    private String ip;
    private int port;
    private Handler mHandler;
    volatile private ClientSocketListener listener;

    public ClientSocketThread(String ip, int port,
                              @Nullable Handler handler, ClientSocketListener listener) throws IOException{
        this.ip = ip;
        this.port = port;
        this.mHandler = handler;
        this.listener = listener;
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = new Socket(ip, port);
            InputStream inStream = socket.getInputStream();
            DataInputStream is = new DataInputStream(inStream);
            while (!isInterrupted()) {
                try {
                    int type = is.readInt();
                    int rows = is.readInt();
                    int cols = is.readInt();
                    int channels = is.readInt();
                    int size = rows * cols * channels;
                    byte[] masBytes = new byte[rows * cols * channels];
                    int len = 0;
                    while (len < size) {
                        len += is.read(masBytes, len, size - len);
                    }
                    final Mat mat = new Mat(rows, cols, type);
                    mat.put(0, 0, masBytes);
                    if (mHandler != null) {
                        mHandler.post(() -> {
                            if (listener != null) {
                                listener.onMatReceived(mat);
                            }
                        });
                    } else {
                        if (listener != null) {
                            listener.onMatReceived(mat);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void clear() {
        listener = null;
    }
}
