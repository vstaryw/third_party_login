package com.vstaryw.common;

import com.google.common.hash.Hashing;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.util.Base64;

/**
 * 加密
 */
public class CryptalUtil {

    private static final String MAC_NAME = "HmacSHA1";
    private static final String ENCODING = "UTF-8";

    public static byte[] HmacSHA1Encrypt(String encryptText,String encryptKey) throws Exception {
        byte[] data = encryptKey.getBytes(ENCODING);
        //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        SecretKey secretKey = new SecretKeySpec(data,MAC_NAME);
        //生成一个指定 Mac 算法 的 Mac 对象
        Mac mac =Mac.getInstance(MAC_NAME);
        //用给定密钥初始化 Mac 对象
        mac.init(secretKey);
        byte[] text = encryptText.getBytes(ENCODING);
        return mac.doFinal(text);
    }

    public static String hash_hmac(String encryptText,String encryptKey) throws Exception{
        byte[] bytes = HmacSHA1Encrypt(encryptText,encryptKey);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String md5(String str){
        return Hashing.md5().hashString(str, Charset.forName("UTF-8")).toString().toLowerCase();
    }
}
