package org.multiplex.domain.dto;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class TimeRangeDto {
    private OffsetDateTime from;
    private OffsetDateTime to;
}
