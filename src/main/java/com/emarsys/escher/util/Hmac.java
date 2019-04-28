package com.emarsys.escher.util;

import com.emarsys.escher.EscherException;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.MessageDigest;

public class Hmac {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String HASH_ALGO = "SHA-256";

    public static String hash(String text) throws EscherException {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGO);
            return new DigestUtils(md).digestAsHex(text).toLowerCase();
        } catch (Exception e) {
            throw new EscherException("Unable to compute hash: " + e.getMessage(), e);
        }
    }

    public static byte[] sign(String hashAlgo, String key, String data) throws EscherException {
        return  sign(hashAlgo, key.getBytes(UTF8), data);
    }


    public static byte[] sign(String hashAlgo, byte[] key, String data) throws EscherException {
        try {
            hashAlgo = "Hmac" + hashAlgo.toUpperCase();
            Mac mac = Mac.getInstance(hashAlgo);
            mac.init(new SecretKeySpec(key, hashAlgo));
            return mac.doFinal(data.getBytes(UTF8));
        } catch (Exception e) {
            throw new EscherException(
                    "Unable to calculate a request signature: "
                            + e.getMessage(), e);
        }
    }
}
