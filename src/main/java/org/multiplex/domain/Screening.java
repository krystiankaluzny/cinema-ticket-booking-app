package org.multiplex.domain;

import lombok.Value;

import java.time.OffsetDateTime;

@Value
class Screening {
    private int id;
    private Movie movie;
    private Room room;
    private OffsetDateTime startScreeningTime;
}
