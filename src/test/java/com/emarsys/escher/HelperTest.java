package com.emarsys.escher;


import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HelperTest extends TestBase {

    @Test
    public void testCalculateSigningKey() throws Exception {
        Config config = Config.create()
                .setHashAlgo("sha256")
                .setAlgoPrefix("AWS4");

        Helper helper = new Helper(config);

        byte[] signingKey = helper.calculateSigningKey(
                "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY",
                createDate(2011, Calendar.SEPTEMBER, 9), "us-east-1/iam/aws4_request"
        );

        assertThat(
                DatatypeConverter.printHexBinary(signingKey).toLowerCase(),
                is("98f1d889fec4f4421adc522bab0ce1f82e6929c262ed15e5a94c90efd1e3b0e7")
        );
    }


    @Test
    public void testSigningParams() throws Exception {
        Date date = createDate(2011, Calendar.MAY, 11, 12, 0, 0);
        Config config = Config.create()
                .setAlgoPrefix("EMS")
                .setHashAlgo("SHA256");

        Helper helper = new Helper(config);

        Map<String, String> params = helper.calculateSigningParams("th3K3y", date, "us-east-1/host/aws4_request", 12345);

        Map<String, String> expectedParams = new HashMap<>();
        expectedParams.put("Algorithm", "EMS-HMAC-SHA256");
        expectedParams.put("Credentials", "th3K3y/20110511/us-east-1/host/aws4_request");
        expectedParams.put("Date", "20110511T120000Z");
        expectedParams.put("Expires", "12345");
        expectedParams.put("SignedHeaders", "host");

        assertThat(params, is(expectedParams));
    }


    @Test
    public void testCanonicalizeWithMoreHeaderThanHeadersToSign() throws Exception {
        TestParam param = parseTestData("get-vanilla");

        TestParam.Request paramRequest = param.getRequest();

        List<EscherRequest.Header> headers = paramRequest.getHeaders()
                .stream()
                .map(header -> new EscherRequest.Header(header.get(0), header.get(1)))
                .collect(Collectors.toList());

        URI uri = new URI("http://" + paramRequest.getHost() + paramRequest.getUrl());

        EscherRequestImpl request = new EscherRequestImpl(paramRequest.getMethod(), uri, headers, paramRequest.getBody());
        request.addHeader("Custom-Header", "should-not-be-signed");

        Helper helper = new Helper(createConfig(param));

        String canonicalized = helper.canonicalize(request, param.getHeadersToSign());

        assertThat(canonicalized, is(param.getExpected().getCanonicalizedRequest()));

    }

}
