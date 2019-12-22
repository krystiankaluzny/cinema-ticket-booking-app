package org.multiplex.domain.exception;

public class InvalidUserNameOrSurnameException extends RuntimeException {

    public InvalidUserNameOrSurnameException(String name, String surname) {
        super("Invalid user name or surname: " + name + " " + surname);
    }
}
