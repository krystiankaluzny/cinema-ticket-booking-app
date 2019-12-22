package org.multiplex.domain.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class ReservationDto {

    private int screeningId;
    private List<SeatToReserveDto> seatsToReserve;
    private BookingUserDto bookingUser;

    @Builder
    @Value
    public static class SeatToReserveDto {
        private int row;
        private int column;
        private ReservationType reservationType;
    }

    @Builder
    @Value
    public static class BookingUserDto {
        private String name;
        private String surname;
    }

    public enum ReservationType {
        ADULT, STUDENT, CHILD
    }

}
