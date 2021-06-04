package com.season.socket.util;

import com.season.socket.entity.SessionModels;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class WCCipher {
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS7Padding";
    private static final String MAC_ALGORITHM = "HmacSHA256";
    public static final WCCipher INSTANCE;

    
    public static SessionModels.WCEncryptionPayload encrypt(byte[] data, byte[] key) throws Exception, NoSuchAlgorithmException {
        byte[] iv = randomBytes(16);
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(1, (Key) keySpec, (AlgorithmParameterSpec) ivSpec);
        byte[] encryptedData = cipher.doFinal(data);
        SessionModels.WCEncryptionPayload result = new SessionModels.WCEncryptionPayload();
        result.hmac = computeHmac(encryptedData, iv, key);
        result.data = ExtensionsKt.toHexString(encryptedData);
        result.iv = ExtensionsKt.toHexString(iv);
        return result;
    }

    
    public static byte[] decrypt( SessionModels.WCEncryptionPayload payload,  byte[] key) throws Exception {

        byte[] data = ExtensionsKt.toByteArray(payload.data);
        byte[] iv = ExtensionsKt.toByteArray(payload.iv);
        String computedHmac = computeHmac(data, iv, key);
        String var6 = payload.hmac;
        boolean var7 = false;
        if (var6 == null) {
            throw new NullPointerException("null cannot be cast to non-null type java.lang.String");
        } else {
            String var10000 = var6.toLowerCase();
            String var10 = var10000;
            if (!var10000.equals(computedHmac)) {
                throw new Exception("Invalid HMAC");
            } else {
                SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                cipher.init(2, (Key) keySpec, (AlgorithmParameterSpec) ivSpec);
                byte[] var11 = cipher.doFinal(data);
                return var11;
            }
        }
    }

    private static String computeHmac(byte[] data, byte[] iv, byte[] key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        byte[] payload = java.util.Arrays.copyOf(data, data.length + iv.length);
        System.arraycopy(iv, 0, payload, data.length, iv.length);
        mac.init((Key) (new SecretKeySpec(key, "HmacSHA256")));
        byte[] var10000 = mac.doFinal(payload);
        return ExtensionsKt.toHexString(var10000);
    }

    private static byte[] randomBytes(int size) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[size];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    private WCCipher() {
    }

    static {
        WCCipher var0 = new WCCipher();
        INSTANCE = var0;
    }
}
