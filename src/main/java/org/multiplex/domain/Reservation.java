package org.multiplex.domain;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.Set;

@Builder
@Value
class Reservation {

    private int id;
    private int screeningId;
    private String bookingUserName;
    private String bookingUserSurname;
    private OffsetDateTime expirationTime;
    private Set<ReservedSeat> reservedSeats;
    private boolean paid;
}

