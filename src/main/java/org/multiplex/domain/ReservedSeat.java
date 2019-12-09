package org.multiplex.domain;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
class ReservedSeat {
    private int row;
    private int column;
    private ReservationType type;
}
