package com.jtool.apiclient.aes;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import java.util.UUID;

public class AES256Cipher {

    public static String genKey(){
        return UUID.randomUUID().toString().substring(0, 16);
    }

    public static String genIv(){
        return UUID.randomUUID().toString().substring(0, 16);
    }

    public static String encrypt(String iv, String key, String text) {
        try {
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
            SecretKeySpec newKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(text.getBytes()));
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            throw new AESException();
        }
    }

    public static String decrypt(String iv, String key, String text) {
        try {
            byte[] textBytes = Base64.getDecoder().decode(text.getBytes());

            AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
            SecretKeySpec newKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
            return new String(cipher.doFinal(textBytes));
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            throw new AESException();
        }
    }
}