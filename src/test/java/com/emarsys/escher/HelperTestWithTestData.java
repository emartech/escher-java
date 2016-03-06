package com.emarsys.escher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class HelperTestWithTestData extends TestBase {

    private Helper helper;
    private String fileName;
    private TestParam param;


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "get-vanilla" },
                { "post-vanilla" },
                { "get-vanilla-query" },
                { "post-vanilla-query" },
                { "get-vanilla-empty-query-key" },
                { "post-vanilla-empty-query-value" },
                { "get-vanilla-query-order-key" },
                { "post-x-www-form-urlencoded" },
                { "post-x-www-form-urlencoded-parameters" },

                { "get-header-value-trim" },
                { "post-header-key-case" },
                { "post-header-key-sort" },
                { "post-header-value-case" },

                { "get-vanilla-query-order-value" },
                { "get-vanilla-query-order-key-case" },
                { "get-unreserved" },
//                { "get-vanilla-query-unreserved" },
//                { "get-vanilla-ut8-query" },
                { "get-utf8" },
                { "get-space" },
//                { "post-vanilla-query-space" },
//                { "post-vanilla-query-nonunreserved" },

//                { "get-slash" },
//                { "get-slashes" },
//                { "get-slash-dot-slash" },
//                { "get-slash-pointless-dot" },
//                { "get-relative" },
//                { "get-relative-relative" },
        });
    }


    public HelperTestWithTestData(String fileName) {
        this.fileName = fileName;
    }


    @Before
    public void setUp() throws Exception {
        param = parseTestData(fileName);

        helper = new Helper(createConfig(param));
    }


    @Test
    public void testCanonicalize() throws Exception {

        TestParam.Request paramRequest = param.getRequest();

        List<EscherRequest.Header> headers = paramRequest.getHeaders()
                .stream()
                .map(header -> new EscherRequest.Header(header.get(0), header.get(1)))
                .collect(Collectors.toList());

        URI uri = new URI("http://" + paramRequest.getHost() + paramRequest.getUrl());

        EscherRequestImpl request = new EscherRequestImpl(paramRequest.getMethod(), uri, headers, paramRequest.getBody());

        String canonicalized = helper.canonicalize(request, param.getHeadersToSign());

        assertThat(fileName, canonicalized, is(param.getExpected().getCanonicalizedRequest()));
    }


    @Test
    public void testCalculateStringToSign() throws Exception {
        String stringToSign = helper.calculateStringToSign(
                getConfigDate(param), param.getConfig().getCredentialScope(),
                param.getExpected().getCanonicalizedRequest()
        );
        assertThat(fileName, stringToSign, is(param.getExpected().getStringToSign()));
    }


    @Test
    public void testCalculateAuthHeader() throws Exception {
        Instant date = getConfigDate(param);
        byte[] signingKey = helper.calculateSigningKey(
                param.getConfig().getApiSecret(),
                date, param.getConfig().getCredentialScope()
        );
        String signature = helper.calculateSignature(signingKey, param.getExpected().getStringToSign());
        String authHeader = helper.calculateAuthHeader(
                param.getConfig().getAccessKeyId(),
                date, param.getConfig().getCredentialScope(),
                param.getHeadersToSign(),
                signature
        );
        assertThat(fileName, authHeader, is(param.getExpected().getAuthHeader()));
    }

}