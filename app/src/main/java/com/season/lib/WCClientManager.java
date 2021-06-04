package com.season.lib;

import com.season.Configure;
import com.season.EthOfflineSign;
import com.season.L;
import com.season.lib.entity.EthereumModels;
import com.season.lib.entity.SessionModels;

import java.util.Arrays;

public class WCClientManager {

    private WCClient client;

    public WCClientManager() {
        client = new WCClient() {
            @Override
            public void onTransaction(long id, EthereumModels.WCEthereumTransaction transaction) {
                onRequestTransaction(id, transaction, new SignCallback() {
                    @Override
                    public void onTransactionSign(long id, String hash) {
                        client.approveRequest(id, hash);
                    }
                });
            }

            @Override
            public void onSessionRequest(long id, SessionModels.WCPeerMeta peerMeta) {
                L.e("TAG", "onSessionRequest");
                onRequestSession(id, new SessionCallback() {

                    @Override
                    public void onWalletSelect(long id, String address) {
                        client.approveSession(id, Arrays.asList(Configure.address));
                    }
                });
            }

            @Override
            protected void onWCOpen(String peerId) {
                onSocketConnect(true);
            }

            @Override
            protected void onFailure(final Throwable e) {
                onSocketConnect(false);
            }
        };
    }

    protected void onSocketConnect(boolean success) {
    }

    protected void onRequestSession(long id, SessionCallback callback) {
        callback.onWalletSelect(id, Configure.address);
    }

    protected void onRequestTransaction(long id, EthereumModels.WCEthereumTransaction transaction, SignCallback callback) {
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

    public void reject(long id, String message){
        client.rejectSession(id, message);
    }

    public void connect(String url) {
        client.connect(url, null);
    }


    public interface SessionCallback {
        void onWalletSelect(long id, String address);
    }

    public interface SignCallback {
        void onTransactionSign(long id, String hash);
    }

}
