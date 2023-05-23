package com.takiku.nettyim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.takiku.im_lib.protocol.IMProtocol;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        findViewById(R.id.btn_tcp_two).setOnClickListener(this);
        findViewById(R.id.btn_tcp_string_two).setOnClickListener(this);
        findViewById(R.id.btn_ws_two).setOnClickListener(this);
        findViewById(R.id.btn_udp_two).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int protocol = IMProtocol.PRIVATE;
        int codecType = 0; //默认的
        switch (v.getId()){
            case R.id.btn_tcp_two:
                protocol = IMProtocol.PRIVATE;
                break;
            case R.id.btn_tcp_string_two:
                protocol = IMProtocol.PRIVATE;
                codecType = 1;
                break;
            case R.id.btn_ws_two:
                protocol = IMProtocol.WEB_SOCKET;
                break;
            case R.id.btn_udp_two:
                protocol = IMProtocol.UDP;
                break;
        }
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.putExtra("protocol",protocol);
        intent.putExtra("codecType",codecType);
        startActivity(intent);
    }
}
