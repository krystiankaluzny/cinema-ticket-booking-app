package org.multiplex.domain.exception;

public class NoSeatToReserveException extends RuntimeException {

    public NoSeatToReserveException() {
        super("At least one seat needs to be reserved");
    }
}
