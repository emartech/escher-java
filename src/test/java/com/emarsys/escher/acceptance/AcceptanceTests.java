package com.emarsys.escher.acceptance;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class AcceptanceTests {

    private static Server server;


    @BeforeClass
    public static void beforeClass() throws Exception {
        server = new Server();
        server.start();
    }


    @AfterClass
    public static void afterClass() throws Exception {
        server.stop();
    }


    @Test
    public void testWrongSignature() throws Exception {
        Client client = new Client();

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
        Client client = new Client();

        HttpRequestBase get = new HttpGet("http://localhost:" + server.getPort() + "/");
        get = client.signRequest(get);

        String response = client.sendRequest(get);

        assertThat(response, is("OK"));
    }

}
