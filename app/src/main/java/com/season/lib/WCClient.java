package com.season.lib;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.season.L;
import com.season.lib.entity.Enums;
import com.season.lib.entity.EthereumModels;
import com.season.lib.entity.JsonRpcModels;
import com.season.lib.entity.SessionModels;
import com.season.lib.lib.Numeric;
import com.season.lib.util.WCCipher;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public abstract class WCClient extends WebSocketListener {
    private static final String TAG = "WC_CLIENT";

    private WebSocket socket;
    private OkHttpClient httpClient;
    private Gson gson;
    private WCSession session;

    public WCClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(7, TimeUnit.SECONDS)
                .readTimeout(7, TimeUnit.SECONDS)
                .writeTimeout(7, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();
        gson = new GsonBuilder()
                .serializeNulls()
                .create();
    }

    private String remotePeerId;
    private String peerId;
    private SessionModels.WCPeerMeta peerMeta;

    public void connect(String url, String remotePeerId) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        L.d(TAG, "==> connect " + url);
        if (session != null && !session.equal(new WCSession(url))) {
            L.d(TAG, "==> connect " + session.topic);
            L.d(TAG, "==> connect " + new WCSession(url).topic);
            JsonRpcModels.JsonRpcRequest request = new JsonRpcModels.JsonRpcRequest();
            request.id = new Date().getTime();
            request.method = Enums.WCMethod.SESSION_UPDATE;
            request.params = java.util.Collections.singletonList(new SessionModels.WCSessionUpdate());

            encryptAndSend(gson.toJson(request));
            socket.close(1000, null);
        }


        peerMeta = new SessionModels.WCPeerMeta();
        peerMeta.name = "??????";
        peerMeta.url = "https://www.alphawallet.com";
        peerMeta.description = "ETHAddress";
        peerMeta.icons = Arrays.asList("https://alphawallet.com/wp-content/themes/alphawallet/img/alphawallet-Lo.svg");

        this.peerId = UUID.randomUUID().toString();
        this.remotePeerId = remotePeerId;
        session = new WCSession(url);
        socket = httpClient.newWebSocket(new Request.Builder().url(session.bridge).build(), this);

    }

    private int chainId;

    private void handleRequest(JsonRpcModels.JsonRpcRequest<JsonArray> request) {
        switch (request.method) {
            case SESSION_REQUEST:
                L.d(TAG, "==> message SESSION_REQUEST");
                List<SessionModels.WCSessionRequest> requestParams = gson.fromJson(request.params, new TypeToken<List<SessionModels.WCSessionRequest>>() {
                }.getType());
                SessionModels.WCSessionRequest requestParam = requestParams.get(0);
                remotePeerId = requestParam.peerId;
                if (requestParam.chainId == null) {
                    chainId = 1;
                } else {
                    chainId = Integer.parseInt(requestParam.chainId);
                }
                onSessionRequest(request.id, requestParam.peerMeta);
                break;
            case SESSION_UPDATE:
                L.d(TAG, "==> message SESSION_UPDATE");
                break;
            case ETH_SIGN:
                L.d(TAG, "==> message ETH_SIGN");
                break;
            case ETH_PERSONAL_SIGN:
                L.d(TAG, "==> message ETH_PERSONAL_SIGN");
                break;
            case ETH_SIGN_TYPE_DATA:
                L.d(TAG, "==> message ETH_SIGN_TYPE_DATA");
                break;
            case ETH_SIGN_TRANSACTION:
                L.d(TAG, "==> message ETH_SIGN_TRANSACTION");
                break;
            case ETH_SEND_TRANSACTION:
                L.d(TAG, "==> message ETH_SEND_TRANSACTION");
                //{"id":1622617690631960,"jsonrpc":"2.0","method":"eth_sendTransaction","params":[{"from":"0xc0b09b78c00ebced69ed1b397f5fb6ad94938441","to":"0xba12222222228d8ba445958a75a0704d566bf2c8","gasPrice":"0x60db88400","gas":"0x1f6aa","value":"0x610eb740268b618","data":"0x52bbbe2900000000000000000000000000000000000000000000000000000000000000e0000000000000000000000000c0b09b78c00ebced69ed1b397f5fb6ad949384410000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c0b09b78c00ebced69ed1b397f5fb6ad94938441000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003db760ae6c888e5be1ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0b09dea16768f0799065c475be02919503cb2a3500020000000000000000001a000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000006b175474e89094c44da98b954eedeac495271d0f0000000000000000000000000000000000000000000000000610eb740268b61800000000000000000000000000000000000000000000000000000000000000c00000000000000000000000000000000000000000000000000000000000000000"}]}

                List<EthereumModels.WCEthereumTransaction> transactionParams = gson.fromJson(request.params, new TypeToken<List<EthereumModels.WCEthereumTransaction>>() {
                }.getType());
                EthereumModels.WCEthereumTransaction transactionParam = transactionParams.get(0);
                onTransaction(request.id, transactionParam);

                break;
            case GET_ACCOUNTS:
                L.d(TAG, "==> message GET_ACCOUNTS");
                break;
        }
    }

    public abstract void onTransaction(long id, EthereumModels.WCEthereumTransaction transaction);

    public abstract void onSessionRequest(long id, SessionModels.WCPeerMeta peerMeta);


    public static byte[] toByteArray(String string) {
        return Numeric.hexStringToByteArray(string);
    }

    private boolean encryptAndSend(String result) {
        try {
            L.d(TAG, "==> message " + result);
            String payload = gson.toJson(WCCipher.encrypt(result.getBytes("UTF-8"), toByteArray(session.key)));
            SessionModels.WCSocketMessage message = new SessionModels.WCSocketMessage();
            message.topic = remotePeerId == null ? session.topic : remotePeerId;
            message.type = Enums.MessageType.PUB;
            message.payload = payload;

            String json = gson.toJson(message);
            L.d(TAG, "==> encryptAndSend:" + json);
            return socket.send(json);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String decryptMessage(String text) throws Exception {
        SessionModels.WCSocketMessage message = gson.fromJson(text, SessionModels.WCSocketMessage.class);
        SessionModels.WCEncryptionPayload encrypted = gson.fromJson(message.payload, SessionModels.WCEncryptionPayload.class);
        return new String(WCCipher.decrypt(encrypted, toByteArray(session.key)), "UTF-8");
    }


    private boolean subscribe(String topic) {
        SessionModels.WCSocketMessage message = new SessionModels.WCSocketMessage(topic, Enums.MessageType.SUB, "");
        String json = gson.toJson(message);
        L.d(TAG, "==> Subscribe:" + json);
        return socket.send(gson.toJson(message));
    }

    private void handleMessage(String payload) throws Exception {
        try {
            JsonRpcModels.JsonRpcRequest<JsonArray> request = gson.fromJson(payload,
                    new TypeToken<JsonRpcModels.JsonRpcRequest<JsonArray>>() {
                    }.getType());
            Enums.WCMethod method = request.method;
            handleRequest(request);
        } catch (Exception e) {
            JsonRpcModels.JsonRpcErrorResponse error = new JsonRpcModels.JsonRpcErrorResponse();
            error.error = JsonRpcModels.JsonRpcError.invalidParams("Invalid parameters");
            encryptAndSend(gson.toJson(error));
        }
    }


    public <T> boolean approveRequest(long id, T result) {
        JsonRpcModels.JsonRpcResponse<T> response = new JsonRpcModels.JsonRpcResponse();
        response.id = id;
        response.result = result;
        return encryptAndSend(gson.toJson(response));
    }

    public boolean approveSession(long id, List<String> accounts) {
        L.d(TAG, "==> approveSession:" + id);

        SessionModels.WCApproveSessionResponse result = new SessionModels.WCApproveSessionResponse();
        result.chainId = chainId;
        result.accounts = accounts;
        result.peerId = peerId;
        result.peerMeta = peerMeta;
        JsonRpcModels.JsonRpcResponse response = new JsonRpcModels.JsonRpcResponse();
        response.id = id;
        response.result = result;
        return encryptAndSend(gson.toJson(response));
    }

    public boolean rejectSession(long id, String message) {
        L.d(TAG, "==> rejectSession:" + id);

        JsonRpcModels.JsonRpcErrorResponse response = new JsonRpcModels.JsonRpcErrorResponse();
        response.id = id;
        response.error = JsonRpcModels.JsonRpcError.serverError(message);

        return encryptAndSend(gson.toJson(response));
    }

    boolean isConnected;

    protected abstract void onWCOpen(String peerId);

    protected abstract void onFailure(Throwable e);

    protected void onPong(String text) {

    }

    /**
     * Invoked when a web socket has been accepted by the remote peer and may begin transmitting
     * messages.
     */
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        L.d(TAG, "<< websocket opened >>");
        isConnected = true;
        // The Session.topic channel is used to listen session request messages only.
        subscribe(session.topic);
        // The peerId channel is used to listen to all messages sent to this httpClient.
        subscribe(peerId);
        onWCOpen(peerId);
    }

    /**
     * Invoked when a text (type {@code 0x1}) message has been received.
     */
    @Override
    public void onMessage(WebSocket webSocket, String text) {
        String decrypted;
        try {
            if (text.equals("ping")) {
                L.d(TAG, "<== pong");
                onPong(text);
                return;
            }
            L.d(TAG, "<== message " + text);
            decrypted = decryptMessage(text);
            L.d(TAG, "<== decrypted " + decrypted);
            handleMessage(decrypted);
        } catch (Exception e) {
            onFailure(e);
        }
    }


    /**
     * Invoked when a binary (type {@code 0x2}) message has been received.
     */
    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
    }

    /**
     * Invoked when the peer has indicated that no more incoming messages will be transmitted.
     */
    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
    }

    /**
     * Invoked when both peers have indicated that no more messages will be transmitted and the
     * connection has been successfully released. No further calls to this listener will be made.
     */
    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
    }

    /**
     * Invoked when a web socket has been closed due to an error reading from or writing to the
     * network. Both outgoing and incoming messages may have been lost. No further calls to this
     * listener will be made.
     */
    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        L.d(TAG, "<== onFailure " + t.getMessage());
        onFailure(t);
    }

}
