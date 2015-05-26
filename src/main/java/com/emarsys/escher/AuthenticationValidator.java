package com.emarsys.escher;

import com.emarsys.escher.util.DateTime;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

class AuthenticationValidator {

    private Config config;


    public AuthenticationValidator(Config config) {
        this.config = config;
    }


    public void validateMandatorySignedHeaders(List<String> signedHeaders, boolean fromHeaders) throws EscherException {
        if (signedHeaders.stream().noneMatch(header -> header.equalsIgnoreCase("host"))) {
            throw new EscherException("Host header is not signed");
        }

        if (fromHeaders && signedHeaders.stream().noneMatch(header -> header.equalsIgnoreCase(config.getDateHeaderName()))) {
            throw new EscherException("Date header is not signed");
        }
    }


    public void validateHashAlgo(String hashAlgo) throws EscherException {
        if (!Arrays.asList("SHA256", "SHA512").contains(hashAlgo.toUpperCase())) {
            throw new EscherException("Only SHA256 and SHA512 hash algorithms are allowed");
        }
    }


    public void validateDates(Date requestDate, Date credentialDate, Date currentTime, int expires) throws EscherException {
        if (!DateTime.sameDay(requestDate, credentialDate)) {
            throw new EscherException("The request date and credential date do not match");
        }

        if (requestDate.before(DateTime.subtractSeconds(currentTime, config.getClockSkew() + expires)) ||
                requestDate.after(DateTime.addSeconds(currentTime, config.getClockSkew()))) {
            throw new EscherException("Request date is not within the accepted time interval");
        }
    }


    public void validateHost(InetSocketAddress address, String hostHeader) throws EscherException {
        boolean defaultPortInUse = address.getPort() == 80 || address.getPort() == 443;
        String expectedHost = address.getHostName() + (defaultPortInUse ? "" : ":" + address.getPort());
        if (!expectedHost.equals(hostHeader)) {
            throw new EscherException("The host header does not match (provided: " + hostHeader + ", expected: " + expectedHost + ")");
        }
    }


    public void validateCredentialScope(String expectedCredentialScope, String actualCredentialScope) throws EscherException {
        if (!actualCredentialScope.equals(expectedCredentialScope)) {
            throw new EscherException("Invalid credentials");
        }
    }


    public void validateSignature(String calculatedSignature, String signature) throws EscherException {
        if (!calculatedSignature.equals(signature)) {
            throw new EscherException("The signatures do not match (provided: " + signature + ", calculated: " + calculatedSignature + ")");
        }
    }

}
