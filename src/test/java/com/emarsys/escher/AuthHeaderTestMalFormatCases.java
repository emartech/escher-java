package com.emarsys.escher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class AuthHeaderTestMalFormatCases {

    private String textToParse;
    private String problem;


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
//              { "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd", "OK" },
                { "E?S-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd", "'?' in algoPrefix" },
                { "EMS-CAMH-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd", "missing 'HMAC'" },
                { "EMS-HMAC-SHA-256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd", "'/' in hashAlgo" },
                { "EMS-HMAC-SHA256 Credentail=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd", "typo in 'Credential'" },
                { "EMS-HMAC-SHA256 Credential=AK!DEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd", "'!' in apiAccessKey" },
                { "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/2011090/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd", "too short date" },
                { "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/11-09-09/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd", "wrong date format" },
                { "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us east 1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd", "space in credentialScope" },
                { "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;123;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd", "numbers in signedHeaders" },
                { "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354axxx49c577a22fd58a190b5fcf8891dbd", "nonhexa characters ('xxx') in signature" },
        });
    }


    public AuthHeaderTestMalFormatCases(String textToParse, String problem) {
        this.textToParse = textToParse;
        this.problem = problem;
    }


    @Test
    public void testParse() throws Exception {
        try {
            AuthHeader.parse(textToParse);
            fail("excpetion should have been thrown - " + problem);
        } catch (EscherException ignored) {}
    }

}