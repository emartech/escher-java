package com.emarsys.escher;


import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TestBase {


    protected TestParam parseTestData(String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File("src/test/fixtures/aws4_testsuite/" + fileName + ".json"), TestParam.class);
    }


    protected Instant getConfigDate(TestParam param) {
        return Instant.parse(param.getConfig().getDate());
    }

    protected static Instant createInstant(int year, int month, int day) {
        return createInstant(year, month, day, 0, 0, 0);
    }


    protected static Instant createInstant(int year, int month, int day, int hourOfDay, int minute, int second) {
        return LocalDateTime.of(year, month, day, hourOfDay, minute, second).toInstant(ZoneOffset.UTC);
    }


    protected Config createConfig(TestParam param) throws Exception{
        return Config.create()
                    .setAlgoPrefix(param.getConfig().getAlgoPrefix())
                    .setHashAlgo(param.getConfig().getHashAlgo());
    }

}
