package com.example.a90678.android2droid1707302025.client;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.a90678.android2droid1707302025.R;

/**
 * Created by 90678 on 2017/7/31.
 */

public class ClientTwoActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    SurfaceView mSv;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_client);
        mSv = (SurfaceView) findViewById(R.id.mSv);
        mSv.getHolder().addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        new ClientTwoService(this, mSv.getHolder().getSurface());
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
