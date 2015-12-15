package com.outsystems.android.core.security;


import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESCipher {

    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    private static final String HASH_KEY = "89ce0c4997af151eee329654e94d4a83367689afd69acb0996f7a62845b1b451";
    private static final String INIT_KEY = "51e310978b74e0e587cadda93dbd7b18";

    private static AESCipher _instance;

    private Cipher cipher;

    private byte[] encryptKey;
    private byte[] initVector;

    public AESCipher() throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException {
        this.cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        this.generateEncryptKey();
        this.generateInitVector();
    }

    public static AESCipher getInstance() throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException {
        if (_instance == null) {
            _instance = new AESCipher();
        }
        return _instance;
    }


    private void generateEncryptKey() throws UnsupportedEncodingException {
        this.encryptKey = new byte[32];

        int length = HASH_KEY.getBytes("UTF-8").length;

        if (HASH_KEY.getBytes("UTF-8").length > encryptKey.length)
            length = encryptKey.length;

        System.arraycopy(HASH_KEY.getBytes("UTF-8"), 0, encryptKey, 0, length);
    }


    private void generateInitVector() throws UnsupportedEncodingException {
        this.initVector = new byte[16];

        int length = INIT_KEY.getBytes("UTF-8").length;

        if(INIT_KEY.getBytes("UTF-8").length > initVector.length)
            length = initVector.length;

        System.arraycopy(INIT_KEY.getBytes("UTF-8"), 0, initVector, 0, length);
    }

    public String encrypt(String inputText) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException {
        SecretKeySpec keySpec = new SecretKeySpec(encryptKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(initVector);

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] results = cipher.doFinal(inputText.getBytes("UTF-8"));

        return Base64.encodeToString(results, Base64.DEFAULT);
    }


    public String decrypt(String encryptedText) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException {
        SecretKeySpec keySpec = new SecretKeySpec(encryptKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(initVector);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decodedValue = Base64.decode(encryptedText.getBytes(), Base64.DEFAULT);
        byte[] decryptedVal = cipher.doFinal(decodedValue);

        return new String(decryptedVal);
    }

}
