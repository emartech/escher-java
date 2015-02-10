package com.emarsys.escher;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.security.MessageDigest;

class Hmac {

    private static final Charset UTF8 = Charset.forName("UTF-8");


    static String hash(String text) throws EscherException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes(UTF8));
            byte[] bytes = md.digest();
            return DatatypeConverter.printHexBinary(bytes).toLowerCase();
        } catch (Exception e) {
            throw new EscherException("Unable to compute hash", e);
        }
    }


    static byte[] sign(String hashAlgo, String key, String data) throws EscherException {
        return  sign(hashAlgo, key.getBytes(UTF8), data);
    }


    static byte[] sign(String hashAlgo, byte[] key, String data) throws EscherException {
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
