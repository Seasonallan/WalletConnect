package com.season.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.season.Configure;
import com.season.myapplication.R;
import com.season.ui.socket.SocketActivity;
import com.season.ui.web.Web3Activity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Web3Activity.class));
            }
        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SocketActivity.class));
            }
        });

        ((TextView) findViewById(R.id.tv)).setText("------------当前配置------------" + "\n" +
                "--链ID: " + Configure.chainId + "\n" +
                "--RPC: " + Configure.rpc + "\n" +
                "--SWAP: " + Configure.dApp + "\n" +
                "--钱包地址: " + Configure.address + "\n" +
                "--钱包私钥: " + Configure.privateKey + "\n" +
                "");


    }

}