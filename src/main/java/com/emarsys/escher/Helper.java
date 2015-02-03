package com.emarsys.escher;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

class Helper {

    private static final char NEW_LINE = '\n';
    public static final Charset UTF8 = Charset.forName("UTF-8");


    static String canonicalize(Request request) throws EscherException {
        return request.getHttpMethod() + NEW_LINE +
                request.getPath() + NEW_LINE +
                "" + NEW_LINE +
                canonicalizeHeaders(request.getHeaders()) + NEW_LINE +
                NEW_LINE +
                signedHeaders(request.getHeaders()) + NEW_LINE +
                hash(request.getBody());
    }

    private static String canonicalizeHeaders(List<String[]> headers) {
        return headers
                .stream()
                .map(array -> array[0].toLowerCase() + ":" + array[1])
                .sorted()
                .reduce((s1, s2) -> s1 + NEW_LINE + s2)
                .get();
    }

    private static String signedHeaders(List<String[]> headers) {
        return headers
                .stream()
                .map(array -> array[0].toLowerCase())
                .sorted()
                .reduce((s1, s2) -> s1 + ';' + s2)
                .get();
    }

    private static String hash(String text) throws EscherException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes(UTF8));
            byte[] bytes = md.digest();
            return DatatypeConverter.printHexBinary(bytes).toLowerCase();
        } catch (Exception e) {
            throw new EscherException("Unable to compute hash", e);
        }
    }


    public static String calculateStringToSign(String credentialScope, String canonicalizedRequest, Date date, String hashAlgo, String algoPrefix) throws EscherException{
        return algoPrefix + "-HMAC-" + hashAlgo + NEW_LINE
                + longDate(date) + NEW_LINE
                + shortDate(date) + "/" + credentialScope + NEW_LINE
                + hash(canonicalizedRequest);
    }


    public static byte[] calculateSigningKey(String secret, Date date, String credentialScope, String hashAlgo, String algoPrefix) throws EscherException{
        byte[] key = (algoPrefix + secret).getBytes(UTF8);

        byte[] data = shortDate(date).getBytes(UTF8);

        key = sign(hashAlgo, key, data);

        for (String credentialPart : credentialScope.split("/")) {
            key = sign(hashAlgo, key, credentialPart.getBytes(UTF8));
        }

        return key;
    }


    private static String longDate(Date date) {
        return new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'").format(date);
    }


    private static String shortDate(Date date) {
        return new SimpleDateFormat("yyyyMMdd").format(date);
    }


    private static byte[] sign(String hashAlgo, byte[] key, byte[] data) throws EscherException {
        try {
            hashAlgo = "Hmac" + hashAlgo.toUpperCase();
            Mac mac = Mac.getInstance(hashAlgo);
            mac.init(new SecretKeySpec(key, hashAlgo));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new EscherException(
                    "Unable to calculate a request signature: "
                            + e.getMessage(), e);
        }
    }


    public static String calculateAuthHeader(String accessKeyId, Date date, String credentialScope, byte[] signingKey, String hashAlgo, String algoPrefix, List<String> signedHeaders, String stringToSign) throws EscherException {
        String signature = DatatypeConverter.printHexBinary(sign(hashAlgo, signingKey, stringToSign.getBytes(UTF8))).toLowerCase();
        return algoPrefix + "-HMAC-" + hashAlgo +
                " Credential=" + accessKeyId + "/" + shortDate(date) + "/" + credentialScope +
                ", SignedHeaders=" + signedHeaders.stream().reduce((s1, s2) -> s1 + ";" + s2).get().toLowerCase() +
                ", Signature=" + signature;
    }
}
