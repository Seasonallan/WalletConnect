package com.season.ui.socket;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.season.Configure;
import com.season.EthOfflineSign;
import com.season.lib.WCClientManager;
import com.season.lib.entity.EthereumModels;
import com.season.myapplication.R;

public class SocketActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);


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
                    new WCClientManager() {
                        @Override
                        protected void onSocketConnect(boolean success) {
                            super.onSocketConnect(success);
                        }

                        @Override
                        protected void onRequestSession(long id, SessionCallback callback) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog alertDialog2 = new AlertDialog.Builder(SocketActivity.this)
                                            .setTitle("????????????")
                                            .setMessage("????????????????????????")
                                            .setIcon(R.mipmap.ic_launcher)
                                            .setPositiveButton("??????", new DialogInterface.OnClickListener() {//??????"Yes"??????
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Toast.makeText(SocketActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                                                    callback.onWalletSelect(id, Configure.address);
                                                }
                                            })

                                            .setNegativeButton("??????", new DialogInterface.OnClickListener() {//????????????
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    reject(id, "????????????");
                                                    Toast.makeText(SocketActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .create();
                                    alertDialog2.show();
                                }
                            });
                        }

                        @Override
                        protected void onRequestTransaction(long id, EthereumModels.WCEthereumTransaction transaction, SignCallback callback) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog alertDialog2 = new AlertDialog.Builder(SocketActivity.this)
                                            .setTitle("????????????")
                                            .setMessage("????????????????????????")
                                            .setIcon(R.mipmap.ic_launcher)
                                            .setPositiveButton("??????", new DialogInterface.OnClickListener() {//??????"Yes"??????
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Toast.makeText(SocketActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                String hash = EthOfflineSign.sign(id, transaction, Configure.privateKey, Configure.address);
                                                                callback.onTransactionSign(id, hash);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }

                                                        }
                                                    }).start();
                                                }
                                            })

                                            .setNegativeButton("??????", new DialogInterface.OnClickListener() {//????????????
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    reject(id, "????????????");
                                                    Toast.makeText(SocketActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .create();
                                    alertDialog2.show();
                                }
                            });
                        }
                    }.connect(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

}