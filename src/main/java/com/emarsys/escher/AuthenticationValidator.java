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

    public void validateMandatorySignedHeaders(AuthElements authElements, List<String> mandatoryHeaders) throws EscherException {
        for (String mandatoryHeader : mandatoryHeaders) {
            if (authElements.getSignedHeaders().stream().noneMatch(header -> header.equalsIgnoreCase(mandatoryHeader))) {
                throw new EscherException("The " + mandatoryHeader.toLowerCase() + " header is not signed" );
            }
        }
    }

    public void validateHTTPMethod(String method) throws EscherException {
        if (!Arrays.asList("GET", "HEAD", "POST", "PUT", "DELETE", "CONNECT", "OPTIONS", "TRACE", "PATCH").contains(method.toUpperCase())) {
            throw new EscherException("The request method is invalid");
        }
    }

    public void validateBody(String method, String body) throws EscherException {
        if (Arrays.asList("POST", "PUT", "PATCH").contains(method.toUpperCase()) && body == null) {
            throw new EscherException("The request body shouldn't be empty if the request method is " + method);
        }
    }

    public void validateHashAlgo(String hashAlgo) throws EscherException {
        if (!Arrays.asList("SHA256", "SHA512").contains(hashAlgo.toUpperCase())) {
            throw new EscherException("Only SHA256 and SHA512 hash algorithms are allowed");
        }
    }

    public void validateDates(Instant requestDate, Instant credentialDate, Instant currentTime, int expires) throws EscherException {
        if (!DateTime.sameDay(requestDate, credentialDate)) {
            throw new EscherException("The credential date does not match with the request date");
        }

        if (requestDate.isBefore(currentTime.minusSeconds(config.getClockSkew() + expires)) ||
                requestDate.isAfter(currentTime.plusSeconds(config.getClockSkew()))) {
            throw new EscherException("The request date is not within the accepted time range");
        }
    }

    public void validateCredentialScope(String expectedCredentialScope, String actualCredentialScope) throws EscherException {
        if (!actualCredentialScope.equals(expectedCredentialScope)) {
            throw new EscherException("The credential scope is invalid");
        }
    }

    public void validateSignature(String calculatedSignature, String signature) throws EscherException {
        if (!calculatedSignature.equals(signature)) {
            throw new EscherException("The signatures do not match");
        }
    }
}
