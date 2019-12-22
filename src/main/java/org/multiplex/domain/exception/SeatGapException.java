package org.multiplex.domain.exception;

public class SeatGapException extends RuntimeException {

    public SeatGapException(int row, int column) {
        super("Reserving seat at row: " + row + " and column: " + column + " makes gap between already reserved seats");
    }

}
