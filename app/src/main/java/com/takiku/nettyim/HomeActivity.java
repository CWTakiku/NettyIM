package com.takiku.nettyim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    private Button button;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        button=findViewById(R.id.btn_two);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, CustomTCPActivity.class));
            }
        });
        findViewById(R.id.btn_ws_two).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, WSActivity.class));
        });
        findViewById(R.id.btn_udp_two).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, UdpActivity.class));
        });
    }
}
