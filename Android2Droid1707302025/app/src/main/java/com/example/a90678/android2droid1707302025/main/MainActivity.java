package com.example.a90678.android2droid1707302025.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.example.a90678.android2droid1707302025.R;
import com.example.a90678.android2droid1707302025.Util.SpUtil;
import com.example.a90678.android2droid1707302025.client.ClientMainActivity;
import com.example.a90678.android2droid1707302025.client.ClientThreeActivity;
import com.example.a90678.android2droid1707302025.client.ClientTwoActivity;
import com.example.a90678.android2droid1707302025.service.ServiceMainActivity;
import com.example.a90678.android2droid1707302025.service.ServiceTwoActivity;

/**
 * Created by 90678 on 2017/7/30.
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        findViewById(R.id.main_client_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ClientMainActivity.class));
            }
        });
        findViewById(R.id.main_client_bt2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ClientTwoActivity.class));
            }
        });
        findViewById(R.id.main_client_bt3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ClientThreeActivity.class));
            }
        });
        findViewById(R.id.main_server_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ServiceMainActivity.class));
            }
        });
        findViewById(R.id.main_server_bt2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ServiceTwoActivity.class));
            }
        });

        EditText etIp = (EditText) findViewById(R.id.main_server_ip_et);
        etIp.setText(SpUtil.getIp(this));
        String ipStr = etIp.getText().toString().trim();
        etIp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                SpUtil.setIp(MainActivity.this, charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}
