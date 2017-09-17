package com.example.a90678.android2droid1707302025.client;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.example.a90678.android2droid1707302025.R;
import com.example.a90678.android2droid1707302025.Util.CircularEncoderBuffer;
import com.example.a90678.android2droid1707302025.Util.SpUtil;
import com.example.a90678.android2droid1707302025.constants.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import static com.example.a90678.android2droid1707302025.constants.Constants.FORMAT;
import static com.example.a90678.android2droid1707302025.constants.Constants.FPS;
import static com.example.a90678.android2droid1707302025.constants.Constants.PHONE_HEIGHT;
import static com.example.a90678.android2droid1707302025.constants.Constants.PHONE_WIDTH;
import static com.example.a90678.android2droid1707302025.constants.Constants.PORT;
import static com.example.a90678.android2droid1707302025.constants.Constants.SERVER;

public class ClientMainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = "ClientMainActivity";
    Socket mSocket;
    private OutputStream mOs;
    private InputStream mIs;

    private SurfaceView mSv;

    MediaCodec mMediaCodec;

    MediaFormat mMediaFormat;

    ByteBuffer[] mInputBuffers;
    int mInputBufferIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main_client);

        mSv = (SurfaceView) findViewById(R.id.mSv);

        mSv.getHolder().addCallback(this);

    }

    private void setData3() {

        while (true) {
            if (mIs != null) {
                try {

                    byte[] buff = new byte[mIs.available()];
                    int le = mIs.read(buff);
                    if (le == -1) {
                        return;
                    }

                    int inIndex = mMediaCodec.dequeueInputBuffer(0);
                    if (inIndex >= 0) {
                        ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(inIndex);
                        inputBuffer.clear();
                        inputBuffer.put(buff);

                        mMediaCodec.queueInputBuffer(inIndex, 0, buff.length, 16, 0);
                    }

//                        http://wenwen.sogou.com/z/q723044135.htm
                    MediaCodec.BufferInfo buffInfo = new MediaCodec.BufferInfo();
                    int outIndex = mMediaCodec.dequeueOutputBuffer(buffInfo, 0);

                    switch (outIndex) {
                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                            break;
                        case MediaCodec.INFO_TRY_AGAIN_LATER:
                            break;
                        case -3:
                            break;
                        default:
                            mMediaCodec.releaseOutputBuffer(outIndex, true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initDecoder() {
        try {
            Log.d(TAG, "初始化解码器...");
            mMediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);

            mMediaFormat =
                    MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, PHONE_WIDTH, PHONE_HEIGHT);

//                    format.setByteBuffer("csd-0", ByteBuffer.wrap(bs));
            mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, Constants.BITRATE);
            mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FPS);
            mMediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 0);
            mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

            mMediaCodec.configure(mMediaFormat, mSv.getHolder().getSurface(),
                    null, 0);
            mMediaCodec.start();

            Log.d(TAG, "解码器创建成功...");

            mInputBuffers = mMediaCodec.getInputBuffers();

            mInputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "连接 Socket ...");
                    mSocket = new Socket(SpUtil.getIp(ClientMainActivity.this), PORT);
                    Log.d(TAG, "Socket 连接成功...");
                    mOs = mSocket.getOutputStream();
                    mIs = mSocket.getInputStream();

                    setData3();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        initDecoder();
        initSocket();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }


}
