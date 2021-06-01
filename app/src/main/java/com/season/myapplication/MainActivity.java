package com.season.myapplication;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.season.myapplication.entity.SessionModels;

import java.util.Arrays;

import wallet.core.jni.Base58;
import wallet.core.jni.CoinType;
import wallet.core.jni.HDWallet;
import wallet.core.jni.PrivateKey;

public class MainActivity extends AppCompatActivity {

    private static final int DEFAULT_KEY_STRENGTH = 128;

    private WCClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.loadLibrary("TrustWalletCore");

        HDWallet newWallet = new HDWallet(DEFAULT_KEY_STRENGTH, "");
        PrivateKey pk = newWallet.getKeyForCoin(CoinType.XRP);


        Log.e("mnemonic", newWallet.mnemonic());

        Log.e("private2", Base58.encode(pk.data()));
        Log.e("address", CoinType.XRP.deriveAddress(pk));


        try {
            String url = "wc:e860b603-50d5-49cd-8459-c0dbe2d97219@1?bridge=https%3A%2F%2Fbridge.walletconnect.org&key=f35c316794a4d143f474d92eb0d3c7a5db1d15816b022b71e7321420ad4bde2a";

            url = "wc:be1e5135-ac8e-4613-8224-1f184bbdcf2e@1?bridge=https%3A%2F%2Fpancakeswap.bridge.walletconnect.org%2F&key=dd072e3736684f64c0ab6d7c42c644068a70f9cd8220924c0b42dc80a6e53fdf";

            client = new WCClient() {
                @Override
                protected void onSessionRequest(long id, SessionModels.WCPeerMeta peerMeta) {
                    try {
                        client.approveSession(Arrays.asList(WCClient.ETHAddress));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                protected void onWCOpen(String peerId) {

                }

                @Override
                protected void onFailure(Throwable e) {

                }
            };
            client.connect(url, null);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}