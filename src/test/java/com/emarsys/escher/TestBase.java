package com.emarsys.escher;


import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class TestBase {

    protected TestParam parseTestData(String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File("src/test/fixtures/aws4_testsuite/" + fileName + ".json"), TestParam.class);
    }

}
