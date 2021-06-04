package com.season.lib;

import com.season.Configure;
import com.season.EthOfflineSign;
import com.season.lib.entity.EthereumModels;
import com.season.lib.entity.SessionModels;

import java.util.Arrays;

public class WCClientManager {

    private WCClient client;

    public WCClientManager() {
        client = new WCClient() {
            @Override
            public void onTransaction(long id, EthereumModels.WCEthereumTransaction transaction) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            client.approveRequest(id, EthOfflineSign.sign(id, transaction, Configure.privateKey, Configure.address));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }

            @Override
            public void onSessionRequest(long id, SessionModels.WCPeerMeta peerMeta) {
                try {
                    client.approveSession(Arrays.asList(Configure.address));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void onWCOpen(String peerId) {

            }

            @Override
            protected void onFailure(final Throwable e) {
            }
        };
    }

    public void connect(String url) {
        client.connect(url, null);
    }

}
