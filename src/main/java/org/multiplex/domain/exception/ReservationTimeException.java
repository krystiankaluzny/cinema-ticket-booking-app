package org.multiplex.domain.exception;

public class ReservationTimeException extends RuntimeException {

    public ReservationTimeException() {
        super("Seats cannot be booked 15 minutes before screening");
    }
}
