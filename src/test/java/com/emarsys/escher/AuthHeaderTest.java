package com.emarsys.escher;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class AuthHeaderTest {

    @Test
    public void testParse() throws Exception {
        String textToParse = "EMS-HMAC-SHA256 Credential=AKID-EXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd";

        AuthHeader header = AuthHeader.parse(textToParse);

        assertEquals("algoPrefix", "EMS", header.getAlgoPrefix());
        assertEquals("hashAlgo", "SHA256", header.getHashAlgo());
        assertEquals("accessKeyId", "AKID-EXAMPLE", header.getAccessKeyId());
        assertEquals("date", "20110909", header.getShortFormatDate());
        assertEquals("credentialScope", "us-east-1/iam/aws4_request", header.getCredentialScope());
        assertEquals("signedHeaders", Arrays.asList("content-type", "host", "x-ems-date"), header.getSignedHeaders());
        assertEquals("signature", "f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd", header.getSignature());
    }

}
