package org.multiplex.domain.exception;

public class SeatNotFoundException extends RuntimeException {

    public SeatNotFoundException(int row, int column, String roomName) {
        super("There is no seat at row: " + row + " and column: " + column + " in " + roomName);
    }
}
