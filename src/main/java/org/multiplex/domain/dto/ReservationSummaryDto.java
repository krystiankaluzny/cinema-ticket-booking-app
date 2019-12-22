package org.multiplex.domain.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
@Value
public class ReservationSummaryDto {
    private int reservationId;
    private BigDecimal totalCost;
    private OffsetDateTime expirationTime;
}
