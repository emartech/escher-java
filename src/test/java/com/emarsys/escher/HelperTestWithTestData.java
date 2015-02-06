package com.emarsys.escher;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class HelperTestWithTestData extends TestBase {

    private Helper helper;
    private String fileName;
    private TestParam param;


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        String[] fileList = new String[] {
                "get-vanilla",
                "post-vanilla",
                "get-vanilla-query",
                "post-vanilla-query",
                "get-vanilla-empty-query-key",
                "post-vanilla-empty-query-value",
                "get-vanilla-query-order-key",
                "post-x-www-form-urlencoded",
                "post-x-www-form-urlencoded-parameters",

                "get-header-value-trim",
                "post-header-key-case",
                "post-header-key-sort",
                "post-header-value-case",

                "get-vanilla-query-order-value",
                "get-vanilla-query-order-key-case",
                "get-unreserved",
//                "get-vanilla-query-unreserved",
//                "get-vanilla-ut8-query",
                "get-utf8",
                "get-space",
//                "post-vanilla-query-space",
//                "post-vanilla-query-nonunreserved",

//                "get-slash",
//                "get-slashes",
//                "get-slash-dot-slash",
//                "get-slash-pointless-dot",
//                "get-relative",
//                "get-relative-relative",
        };

        return Arrays.asList(fileList)
                .stream()
                .map(file -> new String[]{file})
                .collect(Collectors.toList());
    }


    public HelperTestWithTestData(String fileName) {
        this.fileName = fileName;
    }


    @Before
    public void setUp() throws Exception {
        param = parseTestData(fileName);

        helper = new Helper(getConfig(param));
    }


    @Test
    public void testCanonicalize() throws Exception {

        TestParam.Request paramRequest = param.getRequest();

        List<NameValuePair> headers = paramRequest.getHeaders()
                .stream()
                .map(header -> new BasicNameValuePair(header.get(0), header.get(1)))
                .collect(Collectors.toList());

        URI uri = new URI("http://" + paramRequest.getHost() + paramRequest.getUrl());

        RequestImpl request = new RequestImpl(paramRequest.getMethod(), uri, headers, paramRequest.getBody());

        String canonicalised = helper.canonicalize(request);

        assertEquals(fileName, param.getExpected().getCanonicalizedRequest(), canonicalised);
    }


    @Test
    public void testCalculateStringToSign() throws Exception {
        String stringToSign = helper.calculateStringToSign(param.getConfig().getCredentialScope(),
                param.getExpected().getCanonicalizedRequest(),
                getConfigDate(param)
        );
        assertEquals(fileName, param.getExpected().getStringToSign(), stringToSign);
    }


    @Test
    public void testCalculateAuthHeader() throws Exception {
        byte[] signingKey = helper.calculateSigningKey(
                param.getConfig().getApiSecret(),
                getConfigDate(param),
                param.getConfig().getCredentialScope()
        );
        String signature = helper.calculateSignature(signingKey, param.getExpected().getStringToSign());
        String authHeader = helper.calculateAuthHeader(param.getConfig().getAccessKeyId(), getConfigDate(param), param.getConfig().getCredentialScope(), param.getHeadersToSign(), signature);
        assertEquals(fileName, param.getExpected().getAuthHeader(), authHeader);
    }


    private Config getConfig(TestParam param) {
        return Config.create()
                    .setAlgoPrefix(param.getConfig().getAlgoPrefix())
                    .setHashAlgo(param.getConfig().getHashAlgo());
    }

}