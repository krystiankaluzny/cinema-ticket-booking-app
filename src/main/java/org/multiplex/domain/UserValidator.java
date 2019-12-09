package org.multiplex.domain;

interface UserValidator {
    boolean isInvalid(String name, String surname);
}
