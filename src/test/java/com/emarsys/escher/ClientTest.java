package com.emarsys.escher;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ClientTest {
    @Test
    public void testSignedRequest() throws Exception {
        String url = "http://trunk.suite.ett.local/api/v2/internal/214020841/field";
        List<NameValuePair> headers = new ArrayList<>();
        headers.add(new BasicNameValuePair("host", "trunk.suite.ett.local"));
        headers.add(new BasicNameValuePair("Content-Type", ContentType.APPLICATION_JSON.toString()));
        Request escherRequest = new Request("GET", new URI(url), headers, "");

        Request signedRequest = new Escher("eu/suite/ems_request")
                .setAuthHeaderName("X-Ems-Auth")
                .setDateHeaderName("X-Ems-Date")
                .setAlgoPrefix("EMS")
                .signRequest(escherRequest, "key", "secret", Arrays.asList("Content-Type", "X-Ems-Date", "host"));


        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);

        for (NameValuePair header : signedRequest.getHeaders()) {
            request.addHeader(header.getName(), header.getValue());
        }

        HttpResponse response = client.execute(request);

        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        System.out.println(result.toString());
    }
}
