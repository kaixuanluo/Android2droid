package com.example.a90678.android2droid1707302025.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;

import com.example.a90678.android2droid1707302025.R;
import com.example.a90678.android2droid1707302025.constants.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import static com.example.a90678.android2droid1707302025.constants.Constants.FPS;

/**
 * Created by 90678 on 2017/7/30.
 */

public class ServiceMainActivity extends AppCompatActivity {

    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private MediaProjectionManager mediaProjectionManager;
    private MediaCodec encoder;
    private MediaCodec.BufferInfo videoBufferInfo;
    private Surface inputSurface;

    private static String TAG = "ServiceMainActivity";

    ServerSocket mSS;
    Socket mSocket;
    DataOutputStream dos;
    DataInputStream dis;
    OutputStream os;
    InputStream is;

    Intent mResultIntent;
    int mResultCode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_service);

        initSocket();
        requestPermission();
    }

    private void requestPermission() {
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(), Constants.REQUESTCODE);
    }

    public void initSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSS = new ServerSocket(Constants.PORT);
                    mSocket = mSS.accept();
                    is = mSocket.getInputStream();
                    os = mSocket.getOutputStream();
                    dos = new DataOutputStream(os);
                    dis = new DataInputStream(is);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startScreenCapture(int resultCode, Intent resultData) {
        this.mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData);

        Log.d(TAG, "startRecording...");

        this.videoBufferInfo = new MediaCodec.BufferInfo();
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(Constants.FORMAT, Constants.PHONE_WIDTH, Constants.PHONE_HEIGHT);

        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, Constants.BITRATE);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FPS);
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 0);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

        switch (Constants.FORMAT) {
            case MediaFormat.MIMETYPE_VIDEO_AVC:
                Log.d("ServiceMainActivity", "formatAvc");
                // AVC
                mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

                try {
                    this.encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.encoder.setCallback(new MediaCodec.Callback() {
                    @Override
                    public void onInputBufferAvailable(MediaCodec codec, int inputBufferId) {
                    }

                    @Override
                    public void onOutputBufferAvailable(MediaCodec codec, int outputBufferId, MediaCodec.BufferInfo info) {
                        ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferId);
                        if (info.size > 0 && outputBuffer != null) {
                            outputBuffer.position(info.offset);
                            outputBuffer.limit(info.offset + info.size);
                            final byte[] b = new byte[outputBuffer.remaining()];
//                                outputBuffer.get(b);

                            Log.d(TAG, "onOutputBufferAvailable");

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (b == null) {
                                            Log.e(TAG, "byte[] is null...");
                                            return;
                                        }
                                        if (dos == null) {
                                            Log.e(TAG, "dos is null");
                                            return;
                                        }
                                        dos.write(b);
                                        dos.flush();
                                        Log.d("输出", "" + new String(b));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                        if (encoder != null) {
                            encoder.releaseOutputBuffer(outputBufferId, false);
                        }
                    }

                    @Override
                    public void onError(MediaCodec codec, MediaCodec.CodecException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                        Log.i(TAG, "onOutputFormatChanged. CodecInfo:" + codec.getCodecInfo().toString() + " MediaFormat:" + format.toString());
                    }
                });
                break;
        }

        this.encoder.configure(mediaFormat
                , null // surface
                , null // crypto
                , MediaCodec.CONFIGURE_FLAG_ENCODE);

        this.inputSurface = this.encoder.createInputSurface();
        this.encoder.start();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            virtualDisplay = mDisplayManager.createVirtualDisplay("Remote Droid", CodecUtils.WIDTH, CodecUtils.HEIGHT, 50,
//                    this.inputSurface,
//                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC | DisplayManager.VIRTUAL_DISPLAY_FLAG_SECURE);
        } else {
            this.virtualDisplay = this.mediaProjection.createVirtualDisplay("Recording Display",
                    Constants.PHONE_WIDTH, Constants.PHONE_HEIGHT, Constants.DPI, 0, this.inputSurface, null, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mResultIntent = data;
        mResultCode = resultCode;
        Log.d(TAG, "得到返回结果。。。" + resultCode);
        if (requestCode == Constants.REQUESTCODE) {
            if (resultCode == Activity.RESULT_OK) {
                startScreenCapture(resultCode, data);
            } else {

            }
        }
    }
}
