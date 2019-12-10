package org.multiplex.domain.dto;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Builder
@Value
public class AvailableScreeningDto {
    private int screeningId;
    private String movieTitle;
    private OffsetDateTime startScreeningTime;
}
