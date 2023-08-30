package com.emarsys.escher.acceptance;

import com.emarsys.escher.TestBase;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class AcceptanceTests extends TestBase {

    private static Server server;

    private Client client;


    @BeforeClass
    public static void beforeClass() throws Exception {
        server = new Server();
        server.start();
    }


    @AfterClass
    public static void afterClass() throws Exception {
        server.stop();
    }


    @Before
    public void setUp() throws Exception {
        client = new Client();
        setClientTime(Instant.now());
        setServerTime(Instant.now());
    }


    @Test
    public void testWrongSignature() throws Exception {
        HttpRequestBase get = new HttpGet("http://localhost:" + server.getPort() + "/");
        get = client.signRequest(get);

        Header authHeader = get.getFirstHeader("X-Escher-Auth");
        get.removeHeaders("X-Escher-Auth");
        get.addHeader("X-Escher-Auth", authHeader.getValue() + "123");

        String response = client.sendRequest(get);

        assertThat(response, startsWith("The signatures do not match"));
    }


    @Test
    public void testTheSimplestGetRequest() throws Exception {
        HttpRequestBase get = new HttpGet("http://localhost:" + server.getPort() + "/");
        get = client.signRequest(get);

        String response = client.sendRequest(get);

        assertThat(response, is("OK"));
    }


    @Test
    public void testPresignUrlSuccess() throws Exception {
        setClientTime(createInstant(2015, 5, 26, 14,  0, 0));
        setServerTime(createInstant(2015, 5, 26, 14, 20, 0));
        String url = client.presignUrl("http://localhost:" + server.getPort() + "/", 1200);

        HttpRequestBase get = new HttpGet(url);

        String response = client.sendRequest(get);

        assertThat(response, is("OK"));
    }


    @Test
    public void testPresignUrlOutdated() throws Exception {
        setClientTime(createInstant(2015, 1, 26, 14, 0, 0));
        setServerTime(createInstant(2015, 5, 26, 14, 0, 0));
        String url = client.presignUrl("http://localhost:" + server.getPort() + "/", 0);

        HttpRequestBase get = new HttpGet(url);

        String response = client.sendRequest(get);

        assertThat(response, is("Request date is not within the accepted time interval"));
    }


    private void setClientTime(Instant date) {
        client.getClock().setInstant(date);
    }


    private void setServerTime(Instant date) {
        server.getClock().setInstant(date);
    }


    @Test
    public void testPresignUrlWithAnotherUrlInParameter() throws Exception {
        String url = client.presignUrl("http://localhost:" + server.getPort() + "?url=http%3A%2F%2Fexample.com%2Ftest");
        HttpRequestBase get = new HttpGet(url);

        String response = client.sendRequest(get);

        assertThat(response, is("OK"));
    }

}
