package com.season.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.season.myapplication.entity.EthereumModels;
import com.season.myapplication.entity.SessionModels;
import com.season.myapplication.entity.Web3Transaction;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
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
import org.web3j.tx.ChainId;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import wallet.core.jni.CoinType;
import wallet.core.jni.HDWallet;
import wallet.core.jni.PrivateKey;

public class MainActivity extends AppCompatActivity {

    private static final int DEFAULT_KEY_STRENGTH = 128;

    private WCClient client;

    private HDWallet newWallet;

    String privateKey = "68c3370f8208548a88f84efc3f1265c8dd7bf08f46186af1968688609ad74871";
    String address = "0x52d456E7e591d947A0E2002576416d7F4d569a44";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.loadLibrary("TrustWalletCore");

        HDWallet newWallet = new HDWallet(DEFAULT_KEY_STRENGTH, "");
        PrivateKey pk = newWallet.getKeyForCoin(CoinType.ETHEREUM);


        Log.e("tag_mnemonic", newWallet.mnemonic());
        Log.e("tag_address", CoinType.ETHEREUM.deriveAddress(pk));
        Log.i("tag_private", Hex.toHexString(pk.data()));
        Log.i("tag_public", Hex.toHexString(pk.getPublicKeySecp256k1(true).data()));


//        byte[] digest = Hash.keccak256(transaction.data);
//        returnSig.signature = pk.sign(digest, Curve.SECP256K1);
        //0x1b74757b1880f8ef6a07b37c8838647b867e4f2434e0f755b46e2ac3eace01d51213b7809f764924aa2168624c8a8e3716db7c4f0efb8815fc8f219397054f731c

        //http://http-testnet.aitd.io
        client = new WCClient() {
            @Override
            public void onTransaction(long id, EthereumModels.WCEthereumTransaction transaction) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            Web3j web3j = Web3j.build(new HttpService("http://http-testnet.aitd.io"));

                            EthGetTransactionCount c =  web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send();
                            Log.e("TAG", c.getTransactionCount().toString());

                            Web3Transaction w3tx = new Web3Transaction(transaction, id);

                            BigInteger GAS_PRICE = BigInteger.valueOf(0x3b9aca00);
                            BigInteger GAS_LIMIT = BigInteger.valueOf(0x493e0);

                            RawTransaction rtx = RawTransaction.createEtherTransaction(new BigInteger("1"), w3tx.gasPrice, w3tx.gasLimit, w3tx.recipient.toString(), w3tx.value);
                            rtx = RawTransaction.createTransaction(c.getTransactionCount(), GAS_PRICE, GAS_LIMIT, w3tx.recipient.toString(), w3tx.value, w3tx.payload);


                            byte[] bytes = TransactionEncoder.encode(rtx);

                            BigInteger key = new BigInteger(privateKey, 16);
                            if (!WalletUtils.isValidPrivateKey(privateKey))
                                throw new Exception("key error");
                            ECKeyPair keypair = ECKeyPair.create(key);

                            if (true) {
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

                                client.approveRequest(id, txHash);
                                return;
                            }

                            Sign.SignatureData signatureData = Sign.signMessage(
                                    bytes, keypair);
                            byte[] signed = bytesFromSignature(signatureData);
                            signed = patchSignatureVComponent(signed);
                            String signature = Numeric.toHexString(signed);

                            EthSendTransaction raw = web3j
                                    .ethSendRawTransaction(signature)
                                    .send();
                            if (raw.hasError()) {
                                throw new Exception(raw.getError().getMessage());
                            }
                            String txHash = raw.getTransactionHash();

                            client.approveRequest(id, txHash);
                            //client.approveRequest(id, Numeric.toHexString(Numeric.hexStringToByteArray(signature)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }

            @Override
            public void onSessionRequest(long id, SessionModels.WCPeerMeta peerMeta) {
                try {
                    client.approveSession(Arrays.asList(((EditText) findViewById(R.id.et_address)).getText().toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void onWCOpen(String peerId) {

            }

            @Override
            protected void onFailure(final Throwable e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };


        String signJson = "{\"id\":1622710527070562,\"jsonrpc\":\"2.0\",\"method\":\"eth_sendTransaction\",\"params\":[{\"from\":\"0x52d456e7e591d947a0e2002576416d7f4d569a44\",\"to\":\"0xe363cb9f0249d79d480e5b934918f8375df2c1f8\",\"gas\":\"0xbece\",\"data\":\"0x095ea7b3000000000000000000000000d8636f53ea77c78f84ba7856ffb6da896332bb8effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff\"}]}";


        List<EthereumModels.WCEthereumTransaction> transactionParams = null;
        try {
            transactionParams = new Gson().fromJson(new JSONObject(signJson).getJSONArray("params").toString(), new TypeToken<List<EthereumModels.WCEthereumTransaction>>() {
            }.getType());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        EthereumModels.WCEthereumTransaction transactionParam = transactionParams.get(0);
        client.onTransaction(1, transactionParam);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = ((EditText) findViewById(R.id.et)).getText().toString();
                try {
                    String url = text;
                    // url = "wc:3d538c0e-5d8a-4fee-9b6d-f9195b6edaec@1?bridge=https%3A%2F%2Fbridge.walletconnect.org&key=6ca6f22b98850adcc012c5f6351a09ac0bf3f3b2134ea25cd02b7d8a57f7c65b";
                    client.connect(url, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    static byte[] bytesFromSignature(Sign.SignatureData signature) {
        byte[] sigBytes = new byte[65];
        Arrays.fill(sigBytes, (byte) 0);
        try {
            System.arraycopy(signature.getR(), 0, sigBytes, 0, 32);
            System.arraycopy(signature.getS(), 0, sigBytes, 32, 32);
            System.arraycopy(signature.getV(), 0, sigBytes, 64, 1);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return sigBytes;
    }

    /**
     * Patch the 'V'/position component of the signature bytes. The spongy castle signing algorithm returns
     * 0 or 1 for the V component, and although most services accept this some require V to be 27 or 28
     * This just changes 0 or 1 to 0x1b or 0x1c to be universally compatible with all ethereum services.
     * Simple test example: login to 'Chibi Fighters' Dapp (in the discover dapps in the wallet)
     * by signing their challenge. With V = 0 or 1 challenge (ie without this patch)
     * verification will fail, but will pass with V = 0x1b or 0x1c.
     *
     * @param signature
     * @return
     */
    private byte[] patchSignatureVComponent(byte[] signature) {
        if (signature != null && signature.length == 65 && signature[64] < 27) {
            signature[64] = (byte) (signature[64] + (byte) 0x1b);
        }

        return signature;
    }
}