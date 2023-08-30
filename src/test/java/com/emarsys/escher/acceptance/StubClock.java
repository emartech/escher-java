package com.emarsys.escher.acceptance;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class StubClock extends Clock {

    private Instant date = Instant.now();

    @Override
    public ZoneId getZone() {
        return null;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return null;
    }

    @Override
    public Instant instant() {
        return this.date;
    }

    public void setInstant(Instant date) {
        this.date = date;
    }
}
