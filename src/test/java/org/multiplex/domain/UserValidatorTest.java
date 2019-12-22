package org.multiplex.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.multiplex.domain.dto.ReservationDto;
import org.multiplex.domain.exception.InvalidUserNameOrSurnameException;

import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.thenCode;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

class UserValidatorTest {
    private final UserValidator userValidator = new UserValidator();

    private static Stream<Arguments> validUsers() {

        return Stream.of(
                Arguments.of("Jan", "Kowalski"),
                Arguments.of("Wojciech", "Kowalski-Nowak"),
                Arguments.of("Wojciech", "Kowalski-Now"),
                Arguments.of("Wojciech", "Kowalski - Nowak"),
                Arguments.of("Wojciech", "Kowalski -Nowak"),
                Arguments.of("Wojciech", "Kowalski- Nowak")
        );
    }

    @ParameterizedTest
    @MethodSource("validUsers")
    void validate_ValidNameAndSurname(String name, String surname) {

        ReservationDto.BookingUserDto user = ReservationDto.BookingUserDto.builder()
                .name(name)
                .surname(surname)
                .build();

        thenCode(() -> userValidator.validate(user)).doesNotThrowAnyException();
    }

    private static Stream<Arguments> invalidUsers() {

        return Stream.of(
                Arguments.of("Ja", "Kowalski"),
                Arguments.of("Jan", "Ko"),
                Arguments.of("jan", "Kowalski"),
                Arguments.of("Jan", "kowalski"),
                Arguments.of("jan", "kowalski"),
                Arguments.of("Wojciech", "Kowalski-nowak"),
                Arguments.of("Wojciech", "Kowalski-no"),
                Arguments.of("Wojciech", "Kowalski-"),
                Arguments.of("Wojciech", "Kowalski -nowak"),
                Arguments.of("Wojciech", "Kowalski - nowak")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidUsers")
    void validate_InvalidNameAndSurname(String name, String surname) {

        ReservationDto.BookingUserDto user = ReservationDto.BookingUserDto.builder()
                .name(name)
                .surname(surname)
                .build();

        thenThrownBy(() -> userValidator.validate(user)).isInstanceOf(InvalidUserNameOrSurnameException.class);

    }
}
