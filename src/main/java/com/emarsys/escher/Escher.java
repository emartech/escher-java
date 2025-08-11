package com.emarsys.escher;


import com.emarsys.escher.util.DateTime;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

public class Escher {

    public static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";
    public static final int DEFAULT_EXPIRES = 86400;

    private final String credentialScope;
    private final Clock clock;
    private String algoPrefix = "ESR";
    private String vendorKey = "Escher";
    private String hashAlgo = "SHA256";
    private String authHeaderName = "X-Escher-Auth";
    private String dateHeaderName = "X-Escher-Date";
    private int clockSkew = 900;

    public Map<String, String> debugInfo = new HashMap<>();

    public Escher(String credentialScope) {
        this(credentialScope, Clock.systemUTC());
    }

    public Escher(String credentialScope, Clock clock) {
        this.credentialScope = credentialScope;
        this.clock = clock;
    }


    public EscherRequest signRequest(EscherRequest request, String accessKeyId, String secret, List<String> signedHeaders) throws EscherException {
        Instant currentTime = this.clock.instant();
        Config config = createConfig();
        Helper helper = new Helper(config);

        if (!Arrays.asList("GET", "HEAD", "POST", "PUT", "DELETE", "CONNECT", "OPTIONS", "TRACE", "PATCH").contains(request.getHttpMethod().toUpperCase())) {
            throw new EscherException("The request method is invalid");
        }

        helper.addMandatoryHeaders(request, currentTime);
        helper.addMandatorySignedHeaders(signedHeaders);

        if (accessKeyId == null || secret == null) {
            throw new EscherException("Invalid Escher key");
        }
        String signature = calculateSignature(helper, request, secret, signedHeaders, currentTime);
        String authHeader = helper.calculateAuthHeader(accessKeyId, currentTime, credentialScope, signedHeaders, signature);
        debugInfo.put("authHeaderValue", authHeader);

        helper.addAuthHeader(request, authHeader);

        return request;
    }


    public String presignUrl(String url, String accessKeyId, String secret) throws EscherException{
        return presignUrl(url, accessKeyId, secret, DEFAULT_EXPIRES);
    }


    public String presignUrl(String url, String accessKeyId, String secret, int expires) throws EscherException{
        try {
            Instant currentTime = this.clock.instant();
            Config config = createConfig();
            Helper helper = new Helper(config);

            URIBuilder uriBuilder = new URIBuilder(new URI(url));

            Map<String, String> params = helper.calculateSigningParams(accessKeyId, currentTime, credentialScope, expires);
            params.forEach((key, value) -> uriBuilder.addParameter("X-" + vendorKey + "-" + key, value));

            EscherRequest request = new PresignUrlDummyEscherRequest(uriBuilder.build());

            String signature = calculateSignature(helper, request, secret, Arrays.asList("host"), currentTime);

            uriBuilder.addParameter("X-" + vendorKey + "-" + "Signature", signature);

            return uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            throw new EscherException(e);
        }
    }


    public String authenticate(EscherRequest request, Map<String, String> keyDb) throws EscherException {
        return authenticate(request, keyDb, new ArrayList<>());
    }

    public String authenticate(EscherRequest request, Map<String, String> keyDb, List<String> mandatorySignedHeaders) throws EscherException {
        Instant currentTime = this.clock.instant();
        Config config = createConfig();
        Helper helper = new Helper(config);

        AuthElements authElements = helper.parseAuthElements(request);
        Instant requestDate = helper.parseDate(request);
        helper.parseHostHeader(request);

        AuthenticationValidator validator = new AuthenticationValidator(config);

        mandatorySignedHeaders.add("host");
        if (authElements.isFromHeaders()) {
            mandatorySignedHeaders.add(config.getDateHeaderName());
        }
        validator.validateMandatorySignedHeaders(authElements, mandatorySignedHeaders);
        validator.validateHTTPMethod(request.getHttpMethod());
        String secret = retrieveSecret(keyDb, authElements.getAccessKeyId());
        validator.validateBody(request.getHttpMethod(), request.getBody());
        validator.validateHashAlgo(authElements.getHashAlgo());
        validator.validateDates(requestDate, DateTime.parseShortString(authElements.getCredentialDate()), currentTime, authElements.getExpires());
        validator.validateCredentialScope(credentialScope, authElements.getCredentialScope());

        request = authElements.isFromHeaders() ? request : new PresignUrlEscherRequestWrapper(request);
        String calculatedSignature = calculateSignature(helper, request, secret, authElements.getSignedHeaders(), requestDate);

        validator.validateSignature(calculatedSignature, authElements.getSignature());

        return authElements.getAccessKeyId();
    }


    private String retrieveSecret(Map<String, String> keyDb, String accessKeyId) throws EscherException {
        String secret = keyDb.get(accessKeyId);

        if (secret == null) {
            throw new EscherException("Invalid Escher key");
        }
        return secret;
    }


    private String calculateSignature(Helper helper, EscherRequest request, String secret, List<String> signedHeaders, Instant date) throws EscherException {
        String canonicalizedRequest = helper.canonicalize(request, signedHeaders);
        String stringToSign = helper.calculateStringToSign(date, credentialScope, canonicalizedRequest);
        byte[] signingKey = helper.calculateSigningKey(secret, date, credentialScope);
        String signature = helper.calculateSignature(signingKey, stringToSign);

        debugInfo.put("canonicalizedRequest", canonicalizedRequest);
        debugInfo.put("stringToSign", stringToSign);

        return signature;
    }


    private Config createConfig() {
        return Config.create()
                .setVendorKey(vendorKey)
                .setAlgoPrefix(algoPrefix)
                .setHashAlgo(hashAlgo)
                .setDateHeaderName(dateHeaderName)
                .setAuthHeaderName(authHeaderName)
                .setClockSkew(clockSkew);
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
