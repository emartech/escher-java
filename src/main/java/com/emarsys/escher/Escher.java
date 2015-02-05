package com.emarsys.escher;


import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
        request.addHeader(dateHeaderName, "20150204T163500Z");
        String canonicalizedRequest = Helper.canonicalize(request);
        String stringToSign = Helper.calculateStringToSign(credentialScope, canonicalizedRequest, currentTime, hashAlgo, algoPrefix);
        byte[] signingKey = Helper.calculateSigningKey(secret, currentTime, credentialScope, hashAlgo, algoPrefix);
        String authHeader = Helper.calculateAuthHeader(accessKeyId, currentTime, credentialScope, signingKey, hashAlgo, algoPrefix, signedHeaders, stringToSign);

        request.addHeader(authHeaderName, authHeader);

        return request;
    }


    public String presignUrl(String url, String accessKeyId, String secret, int expires) throws EscherException{
        try {
            URI uri = new URI(url);
            URIBuilder uriBuilder = new URIBuilder(uri);

            Map<String, String> params = Helper.calculateSigningParams(algoPrefix, hashAlgo, accessKeyId, currentTime, credentialScope, expires);
            params.forEach((key, value) -> uriBuilder.addParameter("X-" + vendorKey + "-" + key, value));

            ArrayList<NameValuePair> headers = new ArrayList<>();
            headers.add(new BasicNameValuePair("host", uri.getHost()));

            Request request = new Request("GET", uriBuilder.build(), headers, UNSIGNED_PAYLOAD);
            String canonicalizedRequest = Helper.canonicalize(request);
            String stringToSign = Helper.calculateStringToSign(credentialScope, canonicalizedRequest, currentTime, hashAlgo, algoPrefix);
            byte[] signingKey = Helper.calculateSigningKey(secret, currentTime, credentialScope, hashAlgo, algoPrefix);
            String signature = Helper.calculateSignature(hashAlgo, signingKey, stringToSign);

            uriBuilder.addParameter("X-" + vendorKey + "-" + "Signature", signature);

            return uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            throw new EscherException(e);
        }
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
