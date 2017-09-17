package com.example.a90678.android2droid1707302025.client;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.Surface;

import com.example.a90678.android2droid1707302025.constants.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by 90678 on 2017/7/31.
 */

public class ClientTwoService{

    static final int socketServerPORT = Constants.PORT;
    ClientTwoActivity activity;
    ServerSocket serverSocket;
    Socket socket;

    public ClientTwoService(ClientTwoActivity activity, Surface surface) {
        Log.e("constructor()", "called");
        this.activity = activity;
        Thread socketServerThread = new Thread(new SocketServerThread(surface));
        socketServerThread.start();
    }

    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (codecInfo.isEncoder()) {
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    Log.e("codecinfo", codecInfo.getName());
                    return codecInfo;
                }
            }
        }
        return null;
    }

    private class SocketServerThread extends Thread {
        InputStream is;
        Socket socket;
        private MediaCodec codec;
        private Surface surface;

        public SocketServerThread(Surface surface) {
            this.surface = surface;
        }

        @Override
        public void run() {
            Log.e("socketthread", "called");
            try {
                selectCodec("video/avc");
//                codec = MediaCodec.createByCodecName(selectCodec("video/avc").getName());
                codec = MediaCodec.createDecoderByType("video/avc");

                MediaFormat format = MediaFormat.createVideoFormat("video/avc", Constants.PHONE_WIDTH, Constants.PHONE_HEIGHT);
//                  format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
                format.setInteger(MediaFormat.KEY_BIT_RATE, Constants.BITRATE);
                format.setInteger(MediaFormat.KEY_FRAME_RATE, Constants.FPS);
                codec.configure(format, surface, null, 0);
                codec.start();
                serverSocket = new ServerSocket(socketServerPORT);
//                socket = new Socket(Constants.SERVER, Constants.PORT);
                while (true) {
                    socket = serverSocket.accept();
                    Log.e("connection", "accepted");
                    is = socket.getInputStream();
                    if (is != null) {
                        //          File file = new File(Environment.getExternalStorageDirectory() + "/stream.mp4");
                        //              OutputStream output = new FileOutputStream(file);
                        byte[] buff = new byte[1024 * 1024]; // or other buffer size
                        int read;
                        while ((read = is.read(buff)) != -1) {
                            //              output.write(buff, 0, read);
                            if (buff.length == 1)
                                continue;

                            int inIndex = codec.dequeueInputBuffer(10000);
                            if (inIndex >= 0) {
                                ByteBuffer inputBuffer = codec.getInputBuffer(inIndex);
                                inputBuffer.clear();
                                inputBuffer.put(buff);

                                codec.queueInputBuffer(inIndex, 0, buff.length, 16, 0);
                            }

                            MediaCodec.BufferInfo buffInfo = new MediaCodec.BufferInfo();
                            int outIndex = codec.dequeueOutputBuffer(buffInfo, 10000);

                            switch (outIndex) {
                                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                                    break;
                                case MediaCodec.INFO_TRY_AGAIN_LATER:
                                    break;
                                case -3:
                                    break;
                                default:
                                    codec.releaseOutputBuffer(outIndex, true);
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (codec != null) {
                    codec.release();
                }
                if (socket != null) {
                    try {
                        socket.close();
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
