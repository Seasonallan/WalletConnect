package com.season.web;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.season.Configure;
import com.season.EthOfflineSign;
import com.season.L;
import com.season.lib.WCClientManager;
import com.season.lib.entity.EthereumModels;
import com.season.myapplication.R;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class Web3Activity extends AppCompatActivity {

    WebView web3;
    String provderJs, initJs;

    private boolean isInjected;
    private final Object lock = new Object();

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        setTitle("loading...");
        web3 = findViewById(R.id.web3view);

        long time = System.currentTimeMillis();
        provderJs = readRawFile(R.raw.trust);
        initJs = loadInitJs(Configure.chainId, Configure.rpc);
        L.e("TIME", "COST>> " + (System.currentTimeMillis() - time));

        WebView.setWebContentsDebuggingEnabled(true);
        WebSettings web3Setting = web3.getSettings();
        web3Setting.setJavaScriptEnabled(true);
        web3Setting.setDomStorageEnabled(true);

        web3.addJavascriptInterface(new WebAppInterface(), "_tw_");
        web3.setWebViewClient(new WebViewClient() {

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (request == null) {
                    return null;
                }
                String method = request.getMethod();
                String url = request.getUrl().toString();
                L.e("TAG-Intercept", url);
                L.e("TAG-Intercept", method);
                if (method.equalsIgnoreCase("GET") && !request.isForMainFrame()) {
                    if (url.contains(".js") || url.contains("json") || url.contains("css")) {
                        //注入JS 暂时废弃
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                L.e("TAG-shouldOverride", url);
                if (url.startsWith("wc")) {
                    new WCClientManager().connect(url);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                L.e("TAG-onPageStarted", url);
                //loadJs(view);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                L.e("TAG-onPageFinished", url);
                setTitle(view.getTitle());
                if (!isInjected) {
                    synchronized (lock) {
                        loadJs(view);
                        isInjected = true;
                    }
                }
            }
        });

        web3.loadUrl(Configure.dApp);
        web3.requestFocus();
    }

    private void loadJs(WebView view) {
        view.post(new Runnable() {
            @Override
            public void run() {
                view.evaluateJavascript(provderJs, null);
                view.evaluateJavascript(initJs, null);
            }
        });
    }

    class WebAppInterface {

        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void postMessage(String json) {
            L.e("TAG-postMessage", json);
            try {
                JSONObject obj = new JSONObject(json);
                long id = obj.getLong("id");
                String method = obj.getString("name");
                L.e("TAG-request", method);
                switch (method) {
                    case "requestAccounts":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String response = "window.ethereum.sendResponse(" + id + ", [\"" + Configure.address + "\"])";
                                L.e("TAG-response", response);
                                web3.evaluateJavascript(response, null);
                                web3.evaluateJavascript("window.ethereum.setAddress(\"" + Configure.address + "\")", null);
                            }
                        });
                        break;

                    case "signTransaction":

                        JSONObject object = obj.getJSONObject("object");
                        EthereumModels.WCEthereumTransaction transactionParam = new EthereumModels.WCEthereumTransaction();
                        transactionParam.gasPrice = object.getString("gas");
                        transactionParam.from = object.getString("gas");
                        transactionParam.to = object.getString("to");
                        transactionParam.data = object.getString("data");

                        String txHash = EthOfflineSign.sign(id, transactionParam, Configure.privateKey, Configure.address);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                String response = "window.ethereum.sendResponse(" + id + ", [\"" + txHash + "\"])";
                                L.e("TAG-response", response);
                                web3.evaluateJavascript(response, null);
                            }
                        });
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    String loadInitJs(long chainId, String rpcUrl) {
        return "(function() {    var config = {        chainId: " + chainId + ",        rpcUrl: \"" + rpcUrl + "\",        isDebug: true    };    window.ethereum = new trustwallet.Provider(config);    window.web3 = new trustwallet.Web3(window.ethereum);    trustwallet.postMessage = (json) => {        window._tw_.postMessage(JSON.stringify(json));    }})();";
    }

    String readRawFile(int id) {
        String content = "";
        Resources resources = this.getResources();
        InputStream is = null;
        try {
            is = resources.openRawResource(id);
            byte buffer[] = new byte[is.available()];
            is.read(buffer);
            content = new String(buffer);
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return content;
    }


}