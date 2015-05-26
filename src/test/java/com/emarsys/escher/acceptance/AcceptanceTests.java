package com.emarsys.escher.acceptance;

import com.emarsys.escher.TestBase;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

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
        server.getEscher().setCurrentTime(new Date());
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
        client.getEscher().setCurrentTime(createDate(2015, Calendar.MAY, 26, 14,  0, 0));
        server.getEscher().setCurrentTime(createDate(2015, Calendar.MAY, 26, 14, 20, 0));
        String url = client.presignUrl("http://localhost:" + server.getPort() + "/", 1200);

        HttpRequestBase get = new HttpGet(url);

        String response = client.sendRequest(get);

        assertThat(response, is("OK"));
    }

}
