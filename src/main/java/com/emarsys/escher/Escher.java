package com.emarsys.escher;


import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Escher {

    public static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";

    private String credentialScope;
    private String algoPrefix = "ESR";
    private String vendorKey = "Escher";
    private String hashAlgo = "SHA256";
    private Date currentTime = new Date();
    private String authHeaderName = "X-Escher-Auth";
    private String dateHeaderName = "X-Escher-Date";
    private int clockSkew = 900;

    public Escher(String credentialScope) {
        this.credentialScope = credentialScope;
    }


    public Request signRequest(Request request, String accessKeyId, String secret, List<String> signedHeaders) throws EscherException {
        Config config = getConfig();
        Helper helper = new Helper(config);

        helper.addDateHeader(request, currentTime);

        String signature = calculateSignature(request, helper, secret);
        String authHeader = helper.calculateAuthHeader(accessKeyId, currentTime, credentialScope, signedHeaders, signature);

        helper.addAuthHeader(request, authHeader);

        return request;
    }


    public String presignUrl(String url, String accessKeyId, String secret, int expires) throws EscherException{
        try {
            Config config = getConfig();
            Helper helper = new Helper(config);

            URI uri = new URI(url);
            URIBuilder uriBuilder = new URIBuilder(uri);

            Map<String, String> params = helper.calculateSigningParams(accessKeyId, currentTime, credentialScope, expires);
            params.forEach((key, value) -> uriBuilder.addParameter("X-" + vendorKey + "-" + key, value));

            Request request = new PresignUrlDummyRequest(uriBuilder.build());

            String signature = calculateSignature(request, helper, secret);

            uriBuilder.addParameter("X-" + vendorKey + "-" + "Signature", signature);

            return uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            throw new EscherException(e);
        }
    }


    private String calculateSignature(Request request, Helper helper, String secret) throws EscherException {
        String canonicalizedRequest = helper.canonicalize(request);
        String stringToSign = helper.calculateStringToSign(credentialScope, canonicalizedRequest, currentTime);
        byte[] signingKey = helper.calculateSigningKey(secret, currentTime, credentialScope);
        return helper.calculateSignature(signingKey, stringToSign);
    }


    private Config getConfig() {
        return Config.create()
                .setAlgoPrefix(algoPrefix)
                .setHashAlgo(hashAlgo)
                .setDateHeaderName(dateHeaderName)
                .setAuthHeaderName(authHeaderName);
    }


    public Escher setAlgoPrefix(String algoPrefix) {
        this.algoPrefix = algoPrefix;
        return this;
    }


    public Escher setVendorKey(String vendorKey) {
        this.vendorKey = vendorKey;
        return this;
    }


    public Escher setHashAlgo(String hashAlgo) {
        this.hashAlgo = hashAlgo;
        return this;
    }


    public Escher setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
        return this;
    }


    public Escher setAuthHeaderName(String authHeaderName) {
        this.authHeaderName = authHeaderName;
        return this;
    }


    public Escher setDateHeaderName(String dateHeaderName) {
        this.dateHeaderName = dateHeaderName;
        return this;
    }


    public Escher setClockSkew(int clockSkew) {
        this.clockSkew = clockSkew;
        return this;
    }
}
