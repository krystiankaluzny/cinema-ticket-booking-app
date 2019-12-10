package org.multiplex.domain;

import org.multiplex.domain.dto.ReservationDto;
import org.multiplex.domain.exception.InvalidUserNameOrSurnameException;

class UserValidator {

    public void validate(ReservationDto.BookingUserDto bookingUser) {

        if(!isValid(bookingUser.getName()) || !isUserSurnameValid(bookingUser.getSurname())) {
            throw new InvalidUserNameOrSurnameException(bookingUser.getName(), bookingUser.getSurname());
        }
    }

    private boolean isValid(String str) {
        str = str.strip();
        return str.length() >= 3 && Character.isUpperCase(str.codePointAt(0));
    }

    private boolean isUserSurnameValid(String surname) {
        String[] surnameParts = surname.split("-", -1);

        if (surnameParts.length > 2 || surnameParts.length == 0) return false;

        return isValid(surnameParts[0])
                && (surnameParts.length < 2 || isValid(surnameParts[1]));
    }
}
