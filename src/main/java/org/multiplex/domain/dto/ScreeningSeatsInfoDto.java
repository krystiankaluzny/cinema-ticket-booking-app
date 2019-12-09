package org.multiplex.domain.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class ScreeningSeatsInfoDto {
    private ScreeningIdDto screeningId;
    private String roomName;
    private List<AvailableSeatDto> availableSeats;

    @Builder
    @Value
    public static class AvailableSeatDto {
        private int row;
        private int column;
    }
}
