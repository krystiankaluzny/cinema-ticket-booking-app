package org.multiplex.domain.dto;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ScreeningIdDto {
    private int value;

    public static ScreeningIdDto fromInt(int value) {
        return new ScreeningIdDto(value);
    }
}
