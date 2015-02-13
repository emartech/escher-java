package com.emarsys.escher;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(DataProviderRunner.class)
public class AuthHeaderTest {

    @Test
    public void testParseSuccess() throws Exception {
        String textToParse = "EMS-HMAC-SHA256 Credential=AKID-EXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd";

        AuthHeader header = AuthHeader.parse(textToParse);

        assertThat("algoPrefix", header.getAlgoPrefix(), is("EMS"));
        assertThat("hashAlgo", header.getHashAlgo(), is("SHA256"));
        assertThat("accessKeyId", header.getAccessKeyId(), is("AKID-EXAMPLE"));
        assertThat("date", header.getCredentialDate(), is("20110909"));
        assertThat("credentialScope", header.getCredentialScope(), is("us-east-1/iam/aws4_request"));
        assertThat("signedHeaders", Arrays.asList("content-type", "host", "x-ems-date"), is(header.getSignedHeaders()));
        assertThat("signature", header.getSignature(), is("f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd"));
    }


    @Test
    @UseDataProvider("getParseMailFormatCases")
    public void testParseMalFormat(String textToParse, String problem) throws Exception {
        try {
            AuthHeader.parse(textToParse);
            fail("excpetion should have been thrown - " + problem);
        } catch (EscherException ignored) {}
    }


    @DataProvider
    public static Object[][] getParseMailFormatCases() {
        return new Object[][] {
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
        };
    }

}
