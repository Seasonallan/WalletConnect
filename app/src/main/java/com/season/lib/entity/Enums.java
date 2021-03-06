package com.season.lib.entity;

import com.google.gson.annotations.SerializedName;

public class Enums {

    public enum MessageType {
        @SerializedName("pub")
        PUB,
        @SerializedName("sub")
        SUB
    }

    public enum WCMethod {
        @SerializedName("wc_sessionRequest")
        SESSION_REQUEST,

        @SerializedName("wc_sessionUpdate")
        SESSION_UPDATE,

        @SerializedName("eth_sign")
        ETH_SIGN,

        @SerializedName("personal_sign")
        ETH_PERSONAL_SIGN,

        @SerializedName("eth_signTypedData")
        ETH_SIGN_TYPE_DATA,

        @SerializedName("eth_signTransaction")
        ETH_SIGN_TRANSACTION,

        @SerializedName("eth_sendTransaction")
        ETH_SEND_TRANSACTION,

        @SerializedName("get_accounts")
        GET_ACCOUNTS,
    }

}
