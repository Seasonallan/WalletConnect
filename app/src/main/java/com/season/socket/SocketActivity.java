package com.season.socket;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.season.lib.WCClientManager;
import com.season.myapplication.R;

public class SocketActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        byte[] digest = Hash.keccak256(transaction.data);
//        returnSig.signature = pk.sign(digest, Curve.SECP256K1);
        //0x1b74757b1880f8ef6a07b37c8838647b867e4f2434e0f755b46e2ac3eace01d51213b7809f764924aa2168624c8a8e3716db7c4f0efb8815fc8f219397054f731c

        //http://http-testnet.aitd.io


        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = ((EditText) findViewById(R.id.et)).getText().toString();
                try {
                    String url = text;
                    // url = "wc:3d538c0e-5d8a-4fee-9b6d-f9195b6edaec@1?bridge=https%3A%2F%2Fbridge.walletconnect.org&key=6ca6f22b98850adcc012c5f6351a09ac0bf3f3b2134ea25cd02b7d8a57f7c65b";
                    new WCClientManager().connect(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

}