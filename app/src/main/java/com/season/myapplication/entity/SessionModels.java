package com.season.myapplication.entity;

import java.util.List;

public class SessionModels {

    public static class WCPeerMeta {
        public String name;
        public String url;
        public String description;
        public List icons;
    }

    public static class WCSessionRequest {

        public String peerId;

        public WCPeerMeta peerMeta;

        public String chainId;
    }

    public static class WCApproveSessionResponse {
        public boolean approved = true;
        public int chainId;

        public List accounts;

        public String peerId;

        public WCPeerMeta peerMeta;
    }

    public static class WCSessionUpdate {
        public boolean approved;

        public Integer chainId;

        public List accounts;
    }

    public static class WCEncryptionPayload {

        public String data;

        public String hmac;

        public String iv;
    }

    public static class WCSocketMessage {

        public String topic;

        public Enums.MessageType type;

        public String payload;

        public WCSocketMessage(){

        }

        public WCSocketMessage(String topic, Enums.MessageType type, String payload){
            this.topic = topic;
            this.type = type;
            this.payload = payload;
        }

    }

}
