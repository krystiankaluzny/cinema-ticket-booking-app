package org.multiplex.domain;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.multiplex.domain.dto.ReservationDto;
import org.multiplex.domain.exception.NoSeatToReserveException;
import org.multiplex.domain.exception.SeatGapException;
import org.multiplex.domain.exception.SeatNotFoundException;
import org.multiplex.domain.exception.SeatReservedException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.thenCode;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

class SeatsValidatorTest {

    private static final Room TEST_ROOM = new Room(1, "Test", 10, 10);
    private final SeatsValidator seatsValidator = new SeatsValidator();

    private static Stream<Arguments> reservationToValidation() {

        return Stream.of(
                Arguments.of(
                        reservedSeats(1, "oooXXooooo"),
                        seatToReserve(1, "ooXooXoooo"),
                        null),
                Arguments.of(
                        reservedSeats(1, "oooXXooooo"),
                        seatToReserve(1, "oooooooooooooX"),
                        SeatNotFoundException.class),
                Arguments.of(
                        reservedSeats(1, "oooXXooooo"),
                        seatToReserve(20, "oooooooXoo"),
                        SeatNotFoundException.class),
                Arguments.of(
                        reservedSeats(1, "oooXXooooo"),
                        seatToReserve(1, "oXoooooooo"),
                        SeatGapException.class),
                Arguments.of(
                        reservedSeats(1, "oooXXooooo"),
                        seatToReserve(1, "oooXoooooo"),
                        SeatReservedException.class),
                Arguments.of(
                        reservedSeats(1, "oooXXooooo"),
                        seatToReserve(1, "XXXXoooooo"),
                        SeatReservedException.class),
                Arguments.of(
                        reservedSeats(1, "oooXXooooo"),
                        seatToReserve(1, "XXXooooooo"),
                        null),
                Arguments.of(
                        reservedSeats(1, "oooXXooooo"),
                        seatToReserve(1, "oXoooooooo"),
                        SeatGapException.class),
                Arguments.of(
                        reservedSeats(1, "oooXXooooo"),
                        seatToReserve(1, "Xooooooooo"),
                        null),
                Arguments.of(
                        reservedSeats(1, "oooXXooooo"),
                        seatToReserve(1, "XoooooXooo"),
                        SeatGapException.class),
                Arguments.of(
                        reservedSeats(1, "oooXXooooo"),
                        seatToReserve(1, "ooooooXooo"),
                        SeatGapException.class),
                Arguments.of(
                        reservedSeats(1, "oooXXooooo"),
                        seatToReserve(1, "oooooooXoo"),
                        null),
                Arguments.of(
                        reservedSeats(1, "oooXXooooo"),
                        seatToReserve(5, "ooooooXooo"),
                        null),
                Arguments.of(
                        reservedSeats(1, "oooooooooo"),
                        seatToReserve(1, "oooooooXoX"),
                        SeatGapException.class),
                Arguments.of(
                        reservedSeats(1, "oooooXXXoo"),
                        seatToReserve(1, "ooooooooXo"),
                        null),
                Arguments.of(
                        reservedSeats(1, "oooooXXXoo"),
                        seatToReserve(1, "oooooooooo"),
                        NoSeatToReserveException.class)
        );
    }

    @ParameterizedTest
    @MethodSource("reservationToValidation")
    void testValidate(io.vavr.collection.List<Seat> reservedSeats,
                      List<ReservationDto.SeatToReserveDto> seatToReserve,
                      Class<? extends RuntimeException> expectedException) {


        ThrowingCallable throwingCallable = () -> seatsValidator.validate(seatToReserve, reservedSeats, TEST_ROOM);

        if (expectedException == null) {
            thenCode(throwingCallable).doesNotThrowAnyException();
        } else {
            thenThrownBy(throwingCallable).isInstanceOf(expectedException);
        }
    }

    private static io.vavr.collection.List<Seat> reservedSeats(int row, String seats) {
        io.vavr.collection.List<Seat> reserved = io.vavr.collection.List.empty();

        char[] chars = seats.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == 'X') {
                reserved = reserved.append(new Seat(row, i + 1));
            }
        }

        return reserved;
    }

    private static List<ReservationDto.SeatToReserveDto> seatToReserve(int row, String seats) {

        List<ReservationDto.SeatToReserveDto> toReserve = new ArrayList<>();
        char[] chars = seats.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == 'X') {

                toReserve.add(ReservationDto.SeatToReserveDto.builder()
                        .row(row)
                        .column(i + 1)
                        .build());
            }
        }

        return toReserve;
    }
}