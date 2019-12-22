package org.multiplex.domain.exception;

public class SeatReservedException extends RuntimeException {

    public SeatReservedException(int row, int column) {
        super("Seat at row: " + row + " and column: " + column + " is already reserved");
    }
}
