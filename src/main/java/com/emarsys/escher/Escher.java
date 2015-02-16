package com.emarsys.escher;


import com.emarsys.escher.util.DateTime;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
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


    public EscherRequest signRequest(EscherRequest request, String accessKeyId, String secret, List<String> signedHeaders) throws EscherException {
        Config config = createConfig();
        Helper helper = new Helper(config);

        helper.addDateHeader(request);

        String signature = calculateSignature(request, helper, secret, signedHeaders);
        String authHeader = helper.calculateAuthHeader(accessKeyId, credentialScope, signedHeaders, signature);

        helper.addAuthHeader(request, authHeader);

        return request;
    }


    public String presignUrl(String url, String accessKeyId, String secret, int expires) throws EscherException{
        try {
            Config config = createConfig();
            Helper helper = new Helper(config);

            URI uri = new URI(url);
            URIBuilder uriBuilder = new URIBuilder(uri);

            Map<String, String> params = helper.calculateSigningParams(accessKeyId, credentialScope, expires);
            params.forEach((key, value) -> uriBuilder.addParameter("X-" + vendorKey + "-" + key, value));

            EscherRequest request = new PresignUrlDummyEscherRequest(uriBuilder.build());

            String signature = calculateSignature(request, helper, secret, Arrays.asList("host"));

            uriBuilder.addParameter("X-" + vendorKey + "-" + "Signature", signature);

            return uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            throw new EscherException(e);
        }
    }


    private String calculateSignature(EscherRequest request, Helper helper, String secret, List<String> signedHeaders) throws EscherException {
        String canonicalizedRequest = helper.canonicalize(request, signedHeaders);
        String stringToSign = helper.calculateStringToSign(credentialScope, canonicalizedRequest);
        byte[] signingKey = helper.calculateSigningKey(secret, credentialScope);
        return helper.calculateSignature(signingKey, stringToSign);
    }


    private Config createConfig() {
        return Config.create()
                .setAlgoPrefix(algoPrefix)
                .setHashAlgo(hashAlgo)
                .setDateHeaderName(dateHeaderName)
                .setAuthHeaderName(authHeaderName)
                .setDate(currentTime);
    }


    public String authenticate(EscherRequest request, Map<String, String> keyDb, String host) throws EscherException {
        Config config = createConfig();
        Helper helper = new Helper(config);

        AuthHeader authHeader = helper.parseAuthHeader(request);
        Date requestDate = helper.parseDateHeader(request);
        String hostHeader = helper.parseHostHeader(request);

        if (authHeader.getSignedHeaders().stream().noneMatch(header -> header.equalsIgnoreCase("host"))) {
            throw new EscherException("Host header is not signed");
        }

        if (authHeader.getSignedHeaders().stream().noneMatch(header -> header.equalsIgnoreCase(dateHeaderName))) {
            throw new EscherException("Date header is not signed");
        }

        if (!Arrays.asList("SHA256", "SHA512").contains(authHeader.getHashAlgo().toUpperCase())) {
            throw new EscherException("Only SHA256 and SHA512 hash algorithms are allowed");
        }

        Date credentialDate = DateTime.parseShortString(authHeader.getCredentialDate());

        if (!DateTime.sameDay(requestDate, credentialDate)) {
            throw new EscherException("The request date and credential date do not match");
        }

        if (requestDate.before(DateTime.subtractSeconds(currentTime, clockSkew)) ||
                requestDate.after(DateTime.addSeconds(currentTime, clockSkew))) {
            throw new EscherException("Request date is not within the accepted time interval");
        }

        if (!host.equals(hostHeader)) {
            throw new EscherException("The host header does not match");
        }

        if (!credentialScope.equals(authHeader.getCredentialScope())) {
            throw new EscherException("Invalid credentials");
        }

        if (!keyDb.containsKey(authHeader.getAccessKeyId())) {
            throw new EscherException("Invalid access key id");
        }

        config.setDate(requestDate);
        request = new AuthenticationEscherRequest(request, config, host);

        String calculatedSignature = calculateSignature(request, helper, keyDb.get(authHeader.getAccessKeyId()), authHeader.getSignedHeaders());
        if (!calculatedSignature.equals(authHeader.getSignature())) {
            throw new EscherException("The signatures do not match (provided: " + authHeader.getSignature() + ", calculated: " + calculatedSignature + ")");
        }

        return authHeader.getAccessKeyId();
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
