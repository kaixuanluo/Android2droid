package com.example.a90678.android2droid1707302025.client;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import com.example.a90678.android2droid1707302025.constants.Constants;
import com.example.a90678.android2droid1707302025.constants.DeEncodecCommon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 90678 on 2017/8/1.
 */

public class ClientThreeActivity extends AppCompatActivity {

    private static final String TAG = "ServiceTwoActivity";
    private MediaCodec encoder = null;
    private VirtualDisplay virtualDisplay;
    private static final int REQUEST_MEDIA_PROJECTION = 1;

    static MediaProjection mMediaProjection;
    private MediaProjectionManager mMediaProjectionManager;

    private ServerSocket mSS;
    private Socket mSocket;
    private InputStream mIs;
    private OutputStream mOs;
    private DataInputStream mDis;
    private DataOutputStream mDos;

    private List<Socket> mSocketList = new ArrayList<>();

    private EncodedListener el;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initSocket();

        startScreenCapture();

    }

    private void initSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSS = new ServerSocket(Constants.PORT_VIDEO);
                    Socket socket = new Socket("192.168.3.4", Constants.PORT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startScreenCapture() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mMediaProjectionManager = (MediaProjectionManager)
                    getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        }

        startActivityForResult(
                mMediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, "User cancelled the access", Toast.LENGTH_SHORT).show();
                return;
            }
            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);

            startDisplayManager();

            new Thread(new EncoderWorker()).start();

        }
    }

    @TargetApi(19)
    private class EncoderWorker implements Runnable {

        @Override
        public void run() {

            if (mSS == null) {
                return;
            }

            boolean isAccept = true;
            while (isAccept) {
//                try {
//                    Socket socket = mSS.accept();
//                    new Thread(new SendData1()).start();
//                    mSocketList.add(socket);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                try {
                    Log.d(TAG, "连接Socket 中...");
                    mSocket = mSS.accept();
                    mIs = mSocket.getInputStream();
                    mOs = mSocket.getOutputStream();
                    mDis = new DataInputStream(mIs);
                    mDos = new DataOutputStream(mOs);
                    Log.d(TAG, "连接Socket 成功... ");
                    mSocketList.add(mSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (mSocketList.contains(mSocket)) {
                        mSocketList.remove(mSocket);
                    }
                }

                if (mDos == null) {
                    Log.e(TAG, " mDos is null return...");
                    return;
                }

//                new Thread(new SendData1()).start();

                sendData1();
            }

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            }).start();
        }
    }

    private class SendData1 implements Runnable {

        @Override
        public void run() {
            sendData1();
        }
    }

    private void sendData1() {
        ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();

        boolean encoderDone = false;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        String infoString;

        while (!encoderDone) {
            int encoderStatus;
            try {
                encoderStatus = encoder.dequeueOutputBuffer(info, Constants.TIMEOUT_USEC);
            } catch (IllegalStateException e) {
                e.printStackTrace();
                break;
            }

            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                //Log.d(TAG, "no output from encoder available");
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = encoder.getOutputBuffers();
                Log.d(TAG, "encoder output buffers changed");
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // not expected for an encoder
                MediaFormat newFormat = encoder.getOutputFormat();
                Log.d(TAG, "encoder output format changed: " + newFormat);
            } else if (encoderStatus < 0) {
                Log.e(TAG, "encoderStatus < 0");
                continue;
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    Log.d(TAG, "============It's NULL. BREAK!=============");
                    return;
                }

//                        infoString = info.offset + "," + info.size + "," +
//                                info.presentationTimeUs + "," + info.flags;
//                        try {
//                            mDos.write(infoString.getBytes());
//                            Log.d(TAG, "输出 info " + infoString);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

                final byte[] b = new byte[info.size];
                try {
                    if (info.size != 0) {
                        encodedData.limit(info.offset + info.size);
                        encodedData.position(info.offset);
                        encodedData.get(b, info.offset, info.offset + info.size);

//                            if (mSocketList != null && !mSocketList.isEmpty()) {
//                                Log.d(TAG, "界面变化...");
//                                for (final Socket socket : mSocketList) {
//                                    Log.d(TAG, "界面变化...进入循环...");
//                                    if (socket.isConnected() && !socket.isClosed()) {
////                                        new Thread(new Runnable() {
////                                            @Override
////                                            public void run() {
//                                                try {
//                                                    OutputStream os = socket.getOutputStream();
//                                                    os.write(b);
//                                                    os.flush();
//                                                    Log.d(TAG, "输出 ");
//                                                } catch (IOException e) {
//                                                    e.printStackTrace();
//                                                }
////                                            }
////                                        }).start();
//                                    } else {
//                                        mSocketList.remove(socket);
//                                    }
//                                }
//                            }

//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
                        try {
                            if (mDis == null) {
                                return;
                            }
                            mDos.write(b);
                            mDos.flush();
                            Log.d(TAG, "输出 " + new String(b));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                            }
//                        }).start();

                        if (el != null) {
                            el.encoded(b);
                        }
                    }

                } catch (BufferUnderflowException e) {
                    e.printStackTrace();
                }

                encoderDone = (info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;

                try {
                    if (encoder == null) {
                        Log.e("ServerService ", "encoder is null");
                        return;
                    }
                    encoder.releaseOutputBuffer(encoderStatus, false);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public interface EncodedListener {
        void encoded(byte[] bs);
    }

//    private void sendData2() {
//
//        encoder.setCallback(new MediaCodec.Callback() {
//
//            @Override
//            public void onInputBufferAvailable(MediaCodec codec, int inputBufferId) {
//            }
//
//            @Override
//            public void onOutputBufferAvailable(MediaCodec codec, int outputBufferId, MediaCodec.BufferInfo info) {
//                if (mDos == null) {
//                    Log.e(TAG, "socket 未连接...");
//                    if (encoder != null) {
//                        encoder.releaseOutputBuffer(outputBufferId, false);
//                    }
//                    return;
//                }
//                ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferId);
//                Log.d(TAG, "infosize " + info.size + "  outputBuffer " + outputBuffer);
//                if (info.size > 0 && outputBuffer != null) {
//                    outputBuffer.position(info.offset);
//                    outputBuffer.limit(info.offset + info.size);
//                    final byte[] b = new byte[outputBuffer.remaining()];
//                    outputBuffer.get(b);
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                mDos.write(b);
//                                mDos.flush();
//                                Log.d(TAG, "输出 222 " + new String(b));
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }).start();
//                    if (encoder != null) {
//                        encoder.releaseOutputBuffer(outputBufferId, false);
//                    }
////                if (videoBufferInfo != null && (videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
////                    Log.i(TAG, "End of Stream");
////                    stopScreenCapture();
////                }
//                }
//            }
//
//            @Override
//            public void onError(MediaCodec codec, MediaCodec.CodecException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
//                Log.i(TAG, "onOutputFormatChanged. CodecInfo:" + codec.getCodecInfo().toString() + " MediaFormat:" + format.toString());
//            }
//        });
//    }


    @TargetApi(19)
    public void startDisplayManager() {
        DisplayManager mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Surface encoderInputSurface = null;
        try {
            encoderInputSurface = createDisplaySurface();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            virtualDisplay = mDisplayManager.createVirtualDisplay("Remote Droid", Constants.PHONE_WIDTH, Constants.PHONE_HEIGHT, 50,
                    encoderInputSurface,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC | DisplayManager.VIRTUAL_DISPLAY_FLAG_SECURE);
        } else {
            if (mMediaProjection != null) {
                virtualDisplay = mMediaProjection.createVirtualDisplay("Remote Droid",
                        Constants.PHONE_WIDTH, Constants.PHONE_HEIGHT, 50,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        encoderInputSurface, null, null);
            } else {
                Log.e(TAG, "Something went wrong. Please restart the app.");
            }
        }

//        sendData2();
        encoder.start();
    }

    /**
     * Create the display surface out of the encoder. The data to encoder will be fed from this
     * Surface itself.
     *
     * @return
     * @throws IOException
     */
    @TargetApi(19)
    private Surface createDisplaySurface() throws IOException {

        Log.i(TAG, "Starting encoder");
        encoder = DeEncodecCommon.getMediaCodec();
        Surface surface = encoder.createInputSurface();
        return surface;
    }

}
