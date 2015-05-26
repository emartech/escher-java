package com.emarsys.escher;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(DataProviderRunner.class)
public class AuthElementsTest {

    @Test
    public void testParseHeaderSuccess() throws Exception {
        Config config = Config.create()
                .setAlgoPrefix("EMS");
        String textToParse = "EMS-HMAC-SHA256 Credential=AKID-EXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd";

        AuthElements elements = AuthElements.parseHeader(textToParse, config);

        assertThat("hashAlgo", elements.getHashAlgo(), is("SHA256"));
        assertThat("accessKeyId", elements.getAccessKeyId(), is("AKID-EXAMPLE"));
        assertThat("date", elements.getCredentialDate(), is("20110909"));
        assertThat("credentialScope", elements.getCredentialScope(), is("us-east-1/iam/aws4_request"));
        assertThat("signedHeaders", Arrays.asList("content-type", "host", "x-ems-date"), is(elements.getSignedHeaders()));
        assertThat("signature", elements.getSignature(), is("f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd"));
    }


    @Test
    @UseDataProvider("getParseHeaderMailFormatCases")
    public void testParseHeaderMalFormat(String textToParse, String problem) throws Exception {
        Config config = Config.create()
                .setAlgoPrefix("EMS");

        try {
            AuthElements.parseHeader(textToParse, config);
            fail("excpetion should have been thrown - " + problem);
        } catch (EscherException ignored) {}
    }


    @DataProvider
    public static Object[][] getParseHeaderMailFormatCases() {
        return new Object[][] {
//              { "EMS-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd", "OK" },
                { "XXX-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-ems-date, Signature=f36c21c6e16a71a6e8dc56673ad6354aeef49c577a22fd58a190b5fcf8891dbd", "wrong algo prefix" },
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


    @Test
    public void testParseQuerySuccess() throws Exception {
        URI url = new URI("http://example.com?X-TEST-Algorithm=EMS-HMAC-SHA256&X-TEST-Credentials=AKID-EXAMPLE%2F20110909%2Fus-east-1%2Fiam%2Faws4_request&X-TEST-Date=20111009T000000Z&X-TEST-Expires=10&X-TEST-SignedHeaders=host&X-TEST-Signature=191c13fb57e5ff0b8c0ad30fe94f4f0be40b3865916cf6ef084fffd67bae6239");

        Config config = Config.create()
                .setVendorKey("TEST")
                .setAlgoPrefix("EMS");

        AuthElements elements = AuthElements.parseQuery(url, config);

        assertThat("hashAlgo", elements.getHashAlgo(), is("SHA256"));
        assertThat("accessKeyId", elements.getAccessKeyId(), is("AKID-EXAMPLE"));
        assertThat("date", elements.getCredentialDate(), is("20110909"));
        assertThat("credentialScope", elements.getCredentialScope(), is("us-east-1/iam/aws4_request"));
        assertThat("signedHeaders", Arrays.asList("host"), is(elements.getSignedHeaders()));
        assertThat("signature", elements.getSignature(), is("191c13fb57e5ff0b8c0ad30fe94f4f0be40b3865916cf6ef084fffd67bae6239"));
    }


    @Test
    @UseDataProvider("getParseQueryMailFormatCases")
    public void testParseQueryMalFormat(String url, String problem) throws Exception {
        Config config = Config.create()
                .setVendorKey("TEST")
                .setAlgoPrefix("EMS");

        try {
            AuthElements.parseQuery(new URI(url), config);
            fail("excpetion should have been thrown - " + problem);
        } catch (EscherException ignored) {}
    }


    @DataProvider
    public static Object[][] getParseQueryMailFormatCases() {
        return new Object[][] {
//              { "http://example.com?X-TEST-Algorithm=EMS-HMAC-SHA256&X-TEST-Credentials=AKID-EXAMPLE%2F20110909%2Fus-east-1%2Fiam%2Faws4_request&X-TEST-Date=20111009T000000Z&X-TEST-Expires=10&X-TEST-SignedHeaders=host&X-TEST-Signature=191c13fb57e5ff0b8c0ad30fe94f4f0be40b3865916cf6ef084fffd67bae6239", "OK" },
                { "http://example.com?X-TEST-Algorithm=EMS-HMAC-SHA256&X-TEST-Credentials=AKID-EXAMPLE%2F20110909%2Fus-east-1%2Fiam%2Faws4_request&X-TEST-Date=20111009T000000Z&X-TEST-Expires=10&X-TEST-SignedHeaders=host", "missing signature" },
                { "http://example.com?X-OTHER-Algorithm=EMS-HMAC-SHA256&X-OTHER-Credentials=AKID-EXAMPLE%2F20110909%2Fus-east-1%2Fiam%2Faws4_request&X-OTHER-Date=20111009T000000Z&X-OTHER-Expires=10&X-OTHER-SignedHeaders=host&X-TEST-Signature=191c13fb57e5ff0b8c0ad30fe94f4f0be40b3865916cf6ef084fffd67bae6239", "wrong vendor key" },
                { "http://example.com?X-TEST-Algorithm=XXX-HMAC-SHA256&X-TEST-Credentials=AKID-EXAMPLE%2F20110909%2Fus-east-1%2Fiam%2Faws4_request&X-TEST-Date=20111009T000000Z&X-TEST-Expires=10&X-TEST-SignedHeaders=host&X-TEST-Signature=191c13fb57e5ff0b8c0ad30fe94f4f0be40b3865916cf6ef084fffd67bae6239", "wrong algo prefix" },
                { "http://example.com?X-TEST-Algorithm=EMS-HMAC-SHA256&X-TEST-Credentials=AKID-EXAMPLE-20110909-us-east-1-iam-aws4_request&X-TEST-Date=20111009T000000Z&X-TEST-Expires=10&X-TEST-SignedHeaders=host&X-TEST-Signature=191c13fb57e5ff0b8c0ad30fe94f4f0be40b3865916cf6ef084fffd67bae6239", "malformed credentials" }
        };
    }

}
