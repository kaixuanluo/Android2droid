package com.example.a90678.android2droid1707302025.constants;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.io.IOException;

/**
 * Created by 90678 on 2017/8/1.
 */

public class DeEncodecCommon {

    public static MediaFormat getFormat () {
        MediaFormat mMediaFormat = MediaFormat.createVideoFormat(Constants.FORMAT,
                Constants.PHONE_WIDTH, Constants.PHONE_HEIGHT);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, Constants.BITRATE);
        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, Constants.FPS);
        mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        return mMediaFormat;
    }

    public static MediaCodec getMediaCodec () {
        MediaCodec encoder = null;
        try {
            encoder = MediaCodec.createEncoderByType(Constants.FORMAT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        encoder.configure(DeEncodecCommon.getFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        return encoder;
    }
}
