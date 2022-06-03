package com.emarsys.escher;

import com.emarsys.escher.util.DateTime;

import java.time.Instant;
import java.util.Arrays;
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


    public void validateDates(Instant requestDate, Instant credentialDate, Instant currentTime, int expires) throws EscherException {
        if (!DateTime.sameDay(requestDate, credentialDate)) {
            throw new EscherException("The request date and credential date do not match");
        }

        if (requestDate.isBefore(currentTime.minusSeconds(config.getClockSkew() + expires)) ||
                requestDate.isAfter(currentTime.plusSeconds(config.getClockSkew()))) {
            throw new EscherException("Request date is not within the accepted time interval");
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
