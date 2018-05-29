package com.galiazat.diplomtest4opencvimplement.streaming.codecs;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoEncoderThread extends Thread{

    private static final String TAG = "VideoEncoderFromBuffer";

    // parameters for the encoder
    private static final String MIME_TYPE = "video/avc"; // H.264 Advanced Video
    private static final int FRAME_RATE = 25; // 15fps
    private static final int IFRAME_INTERVAL = FRAME_RATE; // 10 between

    private MediaCodec.BufferInfo mBufferInfo; // хранит информацию о текущем буфере
    private MediaCodec mEncoder; // кодер
    private final long mTimeoutUsec; // блокировка в ожидании доступного буфера
    private VideoCoderListener listener;
    private volatile boolean mRunning;
    private volatile byte[] mFrameData;

    public VideoEncoderThread(VideoCoderListener listener) {
        mTimeoutUsec = 10000l;
        this.listener = listener;
    }

    public void setFrameData(byte[] mFrameData) {
        this.mFrameData = mFrameData;
    }

    public void prepare() {
        int width = 1280; // ширина видео
        int height = 720; // высота видео
        int videoBitrate = 3000000; // битрейт видео в bps (бит в секунду)

        mBufferInfo = new MediaCodec.BufferInfo();
        MediaCodecInfo codecInfo = selectCodec(MIME_TYPE);
        if (codecInfo == null) {
            // Don't fail CTS if they don't have an AVC codec (not here,
            // anyway).
            Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }
            Log.d(TAG, "found codec: " + codecInfo.getName());
        int mColorFormat = selectColorFormat(codecInfo, MIME_TYPE);
            Log.d(TAG, "found colorFormat: " + mColorFormat);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,
                width, height);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,
                IFRAME_INTERVAL);
            Log.d(TAG, "format: " + mediaFormat);
        try {
            mEncoder = MediaCodec.createByCodecName(codecInfo.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mEncoder.configure(mediaFormat, null, null,
                MediaCodec.CONFIGURE_FLAG_ENCODE);
        mEncoder.start();
    }

    public void setRunning(boolean running) {
        mRunning = running;
    }

    @Override
    public void run() {
        mRunning = true;
        try {
            while (mRunning) {
                encode();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            release();
        }
    }

    private void encode() {
        byte[] codingData = new byte[1280 * 720 * 3 / 2];
        if (mFrameData == null){
            return;
        }
        NV21toI420SemiPlanar(mFrameData, codingData, 1280, 720);

        ByteBuffer[] inputBuffers = mEncoder.getInputBuffers();
        int inputBufferIndex = mEncoder.dequeueInputBuffer(mTimeoutUsec);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(codingData);
            mEncoder.queueInputBuffer(inputBufferIndex, 0,
                    codingData.length, System.nanoTime() / 1000, 0);
        } else {
            // either all in use, or we timed out during initial setup
            Log.d(TAG, "input buffer not available");
        }

        int status = mEncoder.dequeueOutputBuffer(mBufferInfo, mTimeoutUsec);
        if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
            // нет доступного буфера, пробуем позже
        } else if (status == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            // на случай если кодек меняет буфера
//            outputBuffers = mEncoder.getOutputBuffers();
        } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            MediaFormat codec = mEncoder.getOutputFormat();
            Log.d("Azat", codec.toString());
        } else if (status < 0) {
            // просто ничего не делаем
        } else {
            ByteBuffer[] outputBuffers = mEncoder.getOutputBuffers();
            // статус является индексом буфера кодированных данных
            ByteBuffer data = outputBuffers[status];
            data.position(mBufferInfo.offset);
            data.limit(mBufferInfo.offset + mBufferInfo.size);
            // ограничиваем кодированные данные
            // делаем что-то с данными...
            if (listener != null) {
                Log.d("Azat", "data sent");
                listener.frameCoded(data);
            }
            mEncoder.releaseOutputBuffer(status, false);
            if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
            }
        }
    }

    public void release() {
        listener = null;
        mRunning = false;
        mEncoder.signalEndOfInputStream();
        mEncoder.stop();
        mEncoder.release();
    }

    /**
     * NV21 is a 4:2:0 YCbCr, For 1 NV21 pixel: YYYYYYYY VUVU I420YUVSemiPlanar
     * is a 4:2:0 YUV, For a single I420 pixel: YYYYYYYY UVUV Apply NV21 to
     * I420YUVSemiPlanar(NV12) Refer to https://wiki.videolan.org/YUV/
     */
    private void NV21toI420SemiPlanar(byte[] nv21bytes, byte[] i420bytes,
                                      int width, int height) {
        System.arraycopy(nv21bytes, 0, i420bytes, 0, width * height);
        for (int i = width * height; i < nv21bytes.length; i += 2) {
            i420bytes[i] = nv21bytes[i + 1];
            i420bytes[i + 1] = nv21bytes[i];
        }
    }


    /**
     * Returns a color format that is supported by the codec and by this test
     * code. If no match is found, this throws a test failure -- the set of
     * formats known to the test should be expanded for new platforms.
     */
    private static int selectColorFormat(MediaCodecInfo codecInfo,
                                         String mimeType) {
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo
                .getCapabilitiesForType(mimeType);
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int colorFormat = capabilities.colorFormats[i];
            if (isRecognizedFormat(colorFormat)) {
                return colorFormat;
            }
        }
        Log.e(TAG,
                "couldn't find a good color format for " + codecInfo.getName()
                        + " / " + mimeType);
        return 0; // not reached
    }

    /**
     * Returns true if this is a color format that this test code understands
     * (i.e. we know how to read and generate frames in this format).
     */
    private static boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            // these are the formats we know how to handle for this test
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns the first codec capable of encoding the specified MIME type, or
     * null if no match was found.
     */
    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private static long computePresentationTime(int frameIndex) {
        return 132 + frameIndex * 1000000 / FRAME_RATE;
    }

    /**
     * Returns true if the specified color format is semi-planar YUV. Throws an
     * exception if the color format is not recognized (e.g. not YUV).
     */
    private static boolean isSemiPlanarYUV(int colorFormat) {
        switch (colorFormat) {
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
                return false;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                return true;
            default:
                throw new RuntimeException("unknown format " + colorFormat);
        }
    }

    public interface VideoCoderListener{
        void frameCoded(ByteBuffer byteBuffer);
    }

}
