package com.season;

import android.util.Log;

import com.season.Configure;
import com.season.lib.entity.EthereumModels;
import com.season.lib.entity.Web3Transaction;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class EthOfflineSign {

    public static String sign(long id, EthereumModels.WCEthereumTransaction transactionParam, String privateKey, String address) throws Exception {

        Web3j web3j = Web3j.build(new HttpService(Configure.rpc));

        EthGetTransactionCount c = web3j.ethGetTransactionCount(Configure.address, DefaultBlockParameterName.PENDING).send();
        Log.e("TAG", c.getTransactionCount().toString());

        Web3Transaction w3tx = new Web3Transaction(transactionParam, id);

        BigInteger GAS_PRICE = BigInteger.valueOf(0x3b9aca00);
        BigInteger GAS_LIMIT = BigInteger.valueOf(0x493e0);

        RawTransaction rtx = RawTransaction.createTransaction(c.getTransactionCount(), GAS_PRICE, GAS_LIMIT, w3tx.recipient.toString(), w3tx.value, w3tx.payload);


        BigInteger key = new BigInteger(Configure.privateKey, 16);
        if (!WalletUtils.isValidPrivateKey(Configure.privateKey))
            throw new Exception("key error");
        ECKeyPair keypair = ECKeyPair.create(key);

        Credentials credentials = Credentials.create(keypair);
        byte[] signedMessage = TransactionEncoder.signMessage(rtx, Configure.chainId, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        EthSendTransaction raw = web3j
                .ethSendRawTransaction(hexValue)
                .send();
        if (raw.hasError()) {
            throw new Exception(raw.getError().getMessage());
        }
        return raw.getTransactionHash();
    }

}
