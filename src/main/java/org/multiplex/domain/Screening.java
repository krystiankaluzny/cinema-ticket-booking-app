package org.multiplex.domain;

import lombok.Value;

import java.time.Duration;
import java.time.OffsetDateTime;

@Value
class Screening {
    private int id;
    private Movie movie;
    private Room room;
    private OffsetDateTime startScreeningTime;

    @Value
    static class Movie {
        private int id;
        private String title;
        private Duration duration;
    }

    @Value
    static class Room {
        private int id;
        private String name;
        private int rowCount;
        private int columnCount;
    }
}
