package com.emarsys.escher.demo;

import com.emarsys.escher.Escher;
import com.emarsys.escher.Request;
import com.emarsys.escher.RequestImpl;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientTest {

    public static void main(String... args) {
        try {
            String url = "http://trunk.suite.ett.local/api/v2/internal/114692435/field";
            List<NameValuePair> headers = new ArrayList<>();
            headers.add(new BasicNameValuePair("host", "trunk.suite.ett.local"));
            headers.add(new BasicNameValuePair("Content-Type", ContentType.APPLICATION_JSON.toString()));
            RequestImpl escherRequest = new RequestImpl("GET", new URI(url), headers, "");

            Request signedRequest = new Escher("eu/suite/ems_request")
                    .setAuthHeaderName("X-Ems-Auth")
                    .setDateHeaderName("X-Ems-Date")
                    .setAlgoPrefix("EMS")
                    .signRequest(escherRequest, "examplecode", "nWyoSh8qBBBKwtJ6ocLYTHAEahbV5dkj", Arrays.asList("Content-Type", "X-Ems-Date", "host"));


            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);

            for (Request.Header header : signedRequest.getRequestHeaders()) {
                request.addHeader(header.getFieldName(), header.getFieldValue());
            }

            HttpResponse response = client.execute(request);

            System.out.println("Response Code : "
                    + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            String result = "";
            String line;
            while ((line = rd.readLine()) != null) {
                result += line;
            }

            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
