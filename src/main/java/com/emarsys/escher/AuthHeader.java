package com.emarsys.escher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AuthHeader {

    private String algoPrefix;
    private String hashAlgo;
    private String accessKeyId;
    private String credentialDate;
    private String credentialScope;
    private List<String> signedHeaders = new ArrayList<>();
    private String signature;


    public static AuthHeader parse(String text) throws EscherException {

        Pattern pattern = Pattern.compile("^(?<algoPrefix>\\w+)-HMAC-(?<hashAlgo>[A-Z0-9,]+) Credential=(?<accessKeyId>[\\w\\-]+)/(?<date>\\d{8})/(?<credentialScope>[\\w\\-/]+), SignedHeaders=(?<signedHeaders>[A-Za-z\\-;]+), Signature=(?<signature>[0-9a-f]+)$");
        Matcher matcher = pattern.matcher(text);

        if (matcher.matches()) {
            AuthHeader header = new AuthHeader();

            header.algoPrefix = matcher.group("algoPrefix");
            header.hashAlgo = matcher.group("hashAlgo");
            header.accessKeyId = matcher.group("accessKeyId");
            header.credentialDate = matcher.group("date");
            header.credentialScope = matcher.group("credentialScope");
            header.signedHeaders.addAll(Arrays.asList(matcher.group("signedHeaders").split(";")));
            header.signature = matcher.group("signature");

            return header;
        } else {
            throw new EscherException("Malformed authorization header");
        }

    }


    public String getAlgoPrefix() {
        return algoPrefix;
    }


    public String getHashAlgo() {
        return hashAlgo;
    }


    public String getAccessKeyId() {
        return accessKeyId;
    }


    public String getCredentialDate() {
        return credentialDate;
    }


    public String getCredentialScope() {
        return credentialScope;
    }


    public List<String> getSignedHeaders() {
        return signedHeaders;
    }


    public String getSignature() {
        return signature;
    }
}
