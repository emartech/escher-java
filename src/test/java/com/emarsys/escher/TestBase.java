package com.emarsys.escher;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TestBase {

    protected static Instant createInstant(int year, int month, int day) {
        return createInstant(year, month, day, 0, 0, 0);
    }


    protected static Instant createInstant(int year, int month, int day, int hourOfDay, int minute, int second) {
        return LocalDateTime.of(year, month, day, hourOfDay, minute, second).toInstant(ZoneOffset.UTC);
    }

}
