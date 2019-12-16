package org.multiplex.domain.exception;

public class ScreeningNotFoundException extends RuntimeException {

    public ScreeningNotFoundException(int id) {
        super("Screening with id: " + id + " doesn't exist");
    }
}
