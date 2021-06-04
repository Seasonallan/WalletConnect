package com.season.web;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.season.myapplication.R;
import com.season.socket.entity.EthereumModels;
import com.season.socket.entity.Web3Transaction;

import org.json.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.season.socket.MainActivity.bytesFromSignature;
import static com.season.socket.MainActivity.patchSignatureVComponent;

public class Web3Activity extends AppCompatActivity {

    String privateKey = "68c3370f8208548a88f84efc3f1265c8dd7bf08f46186af1968688609ad74871";
    String address = "0x52d456E7e591d947A0E2002576416d7F4d569a44";

    WebView web3;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        web3 = findViewById(R.id.web3view);


        String provderJs = readRawFile(R.raw.trust);
        String initJs = loadInitJs(239, "http://http-testnet.aitd.io");

        WebView.setWebContentsDebuggingEnabled(true);
        WebSettings web3Setting = web3.getSettings();
        web3Setting.setJavaScriptEnabled(true);
        web3Setting.setDomStorageEnabled(true);

        web3.addJavascriptInterface(new WebAppInterface(), "_tw_");
        web3.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.e("TAG-onPageStarted", url);

                view.evaluateJavascript(provderJs, null);
                view.evaluateJavascript(initJs, null);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.e("TAG-onPageFinished", url);
            }
        });

        //web3.loadUrl("http://192.168.5.131:3000/");
        web3.loadUrl("http://192.168.5.131:3000/#/swap");
        web3.requestFocus();
    }

    class WebAppInterface {

        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void postMessage(String json) {
            Log.e("TAG-postMessage", json);
            try {
                JSONObject obj = new JSONObject(json);
                long id = obj.getLong("id");
                String method = obj.getString("name");
                Log.e("TAG-request", method);
                switch (method) {
                    case "requestAccounts":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String response = "window.ethereum.sendResponse(" + id + ", [\"" + address + "\"])";
                                Log.e("TAG-response", response);
                                web3.evaluateJavascript(response, null);
                                web3.evaluateJavascript("window.ethereum.setAddress(\""+address+"\")", null);
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


                        Web3j web3j = Web3j.build(new HttpService("http://http-testnet.aitd.io"));

                        EthGetTransactionCount c = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send();
                        Log.e("TAG", c.getTransactionCount().toString());

                        Web3Transaction w3tx = new Web3Transaction(transactionParam, id);

                        BigInteger GAS_PRICE = BigInteger.valueOf(0x3b9aca00);
                        BigInteger GAS_LIMIT = BigInteger.valueOf(0x493e0);

                        RawTransaction rtx = RawTransaction.createEtherTransaction(new BigInteger("1"), w3tx.gasPrice, w3tx.gasLimit, w3tx.recipient.toString(), w3tx.value);
                        rtx = RawTransaction.createTransaction(c.getTransactionCount(), GAS_PRICE, GAS_LIMIT, w3tx.recipient.toString(), w3tx.value, w3tx.payload);


                        BigInteger key = new BigInteger(privateKey, 16);
                        if (!WalletUtils.isValidPrivateKey(privateKey))
                            throw new Exception("key error");
                        ECKeyPair keypair = ECKeyPair.create(key);

                        Credentials credentials = Credentials.create(keypair);
                        byte[] signedMessage = TransactionEncoder.signMessage(rtx, 239, credentials);
                        String hexValue = Numeric.toHexString(signedMessage);
                        EthSendTransaction raw = web3j
                                .ethSendRawTransaction(hexValue)
                                .send();
                        if (raw.hasError()) {
                            Log.e("ERROR", raw.getError().getCode() + "");
                            Log.e("ERROR", raw.getError().getData() + "");
                            Log.e("ERROR", raw.getError().getMessage() + "");
                            throw new Exception(raw.getError().getMessage());
                        }
                        String txHash = raw.getTransactionHash();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                String response = "window.ethereum.sendResponse(" + id + ", [\"" + txHash + "\"])";
                                //response = "window.ethereum.sendResponse("+id+", [\"0x6b82b493f1457c99d102786c6fba3b9c88ae62d457cafe16ad220fcae136ab21\"])";
                                Log.e("TAG-response", response);
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

    String loadInitJs(int chainId, String rpcUrl) {
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


    /* Required for CORS requests */
    private Map<String, String> getWeb3Headers() {
        //headers
        return new HashMap<String, String>() {{
            put("Connection", "close");
            put("Content-Type", "text/plain");
            put("Access-Control-Allow-Origin", "*");
            put("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
            put("Access-Control-Max-Age", "600");
            put("Access-Control-Allow-Credentials", "true");
            put("Access-Control-Allow-Headers", "accept, authorization, Content-Type");
        }};
    }


}