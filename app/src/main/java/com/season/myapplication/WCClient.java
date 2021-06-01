package com.season.myapplication;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.season.myapplication.entity.Enums;
import com.season.myapplication.entity.JsonRpcModels;
import com.season.myapplication.entity.SessionModels;
import com.season.myapplication.lib.Numeric;
import com.season.myapplication.util.WCCipher;

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
    public static String ETHAddress = "0xc0b09b78c00EBCeD69eD1b397F5FB6AD94938441";

    public void connect(String url, String remotePeerId) throws Exception {
        Log.d(TAG, "==> connect " + url);
        if (session != null && session.topic != new WCSession(url).topic) {
            JsonRpcModels.JsonRpcRequest request = new JsonRpcModels.JsonRpcRequest();
            request.id = new Date().getTime();
            request.method = Enums.WCMethod.SESSION_UPDATE;
            request.params = java.util.Collections.singletonList(new SessionModels.WCSessionUpdate());

            encryptAndSend(gson.toJson(request));
            socket.close(1000, null);
        }


        peerMeta = new SessionModels.WCPeerMeta();
        peerMeta.name = "特斯";
        peerMeta.url = "https://www.alphawallet.com";
        peerMeta.description = ETHAddress;
        peerMeta.icons = Arrays.asList("https://alphawallet.com/wp-content/themes/alphawallet/img/alphawallet-logo.svg");

        this.peerId = UUID.randomUUID().toString();
        this.remotePeerId = remotePeerId;
        session = new WCSession(url);
        socket = httpClient.newWebSocket(new Request.Builder().url(session.bridge).build(), this);

    }

    private long handshakeId;
    private int chainId;

    private void handleRequest(JsonRpcModels.JsonRpcRequest<JsonArray> request) {
        switch (request.method) {
            case SESSION_REQUEST:
                Log.d(TAG, "==> message SESSION_REQUEST");
                List<SessionModels.WCSessionRequest> params = gson.fromJson(request.params, new TypeToken<List<SessionModels.WCSessionRequest>>() {
                }.getType());
                SessionModels.WCSessionRequest param = params.get(0);
                handshakeId = request.id;
                remotePeerId = param.peerId;
                if (param.chainId == null) {
                    chainId = 1;
                } else {
                    chainId = Integer.parseInt(param.chainId);
                }
                onSessionRequest(request.id, param.peerMeta);
                break;
            case SESSION_UPDATE:
                Log.d(TAG, "==> message SESSION_UPDATE");
                break;
            case ETH_SIGN:
                Log.d(TAG, "==> message ETH_SIGN");
                break;
            case ETH_PERSONAL_SIGN:
                Log.d(TAG, "==> message ETH_PERSONAL_SIGN");
                break;
            case ETH_SIGN_TYPE_DATA:
                Log.d(TAG, "==> message ETH_SIGN_TYPE_DATA");
                break;
            case ETH_SIGN_TRANSACTION:
                Log.d(TAG, "==> message ETH_SIGN_TRANSACTION");
                break;
            case ETH_SEND_TRANSACTION:
                Log.d(TAG, "==> message ETH_SEND_TRANSACTION");
                break;
            case GET_ACCOUNTS:
                Log.d(TAG, "==> message GET_ACCOUNTS");
                break;
        }
    }

    protected abstract void onSessionRequest(long id, SessionModels.WCPeerMeta peerMeta);


    public static byte[] toByteArray(String string) {
        return Numeric.hexStringToByteArray(string);
    }

    private boolean encryptAndSend(String result) throws Exception {
        Log.d(TAG, "==> message " + result);
        String payload = gson.toJson(WCCipher.encrypt(result.getBytes("UTF-8"), toByteArray(session.key)));
        SessionModels.WCSocketMessage message = new SessionModels.WCSocketMessage();
        message.topic = remotePeerId == null ? session.topic : remotePeerId;
        message.type = Enums.MessageType.PUB;
        message.payload = payload;

        String json = gson.toJson(message);
        Log.d(TAG, "==> encryptAndSend:" + json);
        return socket.send(json);
    }

    private String decryptMessage(String text) throws Exception {
        SessionModels.WCSocketMessage message = gson.fromJson(text, SessionModels.WCSocketMessage.class);
        SessionModels.WCEncryptionPayload encrypted = gson.fromJson(message.payload, SessionModels.WCEncryptionPayload.class);
        return new String(WCCipher.decrypt(encrypted, toByteArray(session.key)), "UTF-8");
    }


    private boolean subscribe(String topic) {
        SessionModels.WCSocketMessage message = new SessionModels.WCSocketMessage(topic, Enums.MessageType.SUB, "");
        String json = gson.toJson(message);
        Log.d(TAG, "==> Subscribe:" + json);
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

    public boolean approveSession(List<String> accounts) throws Exception {
        Log.d(TAG, "==> approveSession:" + handshakeId);

        SessionModels.WCApproveSessionResponse result = new SessionModels.WCApproveSessionResponse();
        result.chainId = chainId;
        result.accounts = accounts;
        result.peerId = peerId;
        result.peerMeta = peerMeta;
        JsonRpcModels.JsonRpcResponse response = new JsonRpcModels.JsonRpcResponse();
        response.id = handshakeId;
        response.result = result;
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
        Log.d(TAG, "<< websocket opened >>");
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
                Log.d(TAG, "<== pong");
                onPong(text);
                return;
            }
            Log.d(TAG, "<== message " + text);
            decrypted = decryptMessage(text);
            Log.d(TAG, "<== decrypted " + decrypted);
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
    }

}
