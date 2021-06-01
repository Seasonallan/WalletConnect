package com.season.myapplication;

import android.net.Uri;

public class WCSession {
    public String topic;
    public String version;
    public String bridge;
    public String key;

    public WCSession(String url) {
        if (url.startsWith("wc:")) {
            String uriString = url.replace("wc:", "wc://");
            Uri uri = Uri.parse(uriString);
            bridge = uri.getQueryParameter("bridge");
            key = uri.getQueryParameter("key");
            topic = uri.getUserInfo();
            version = uri.getHost();
        }
    }
}