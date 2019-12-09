package org.multiplex.domain;

import org.multiplex.domain.dto.AvailableScreeningDto;
import org.multiplex.domain.dto.TimeRangeDto;

import java.util.List;
import java.util.stream.Collectors;

public class CinemaService {

    private final ScreeningRepository screeningRepository;

    CinemaService(ScreeningRepository screeningRepository) {
        this.screeningRepository = screeningRepository;
    }

    public List<AvailableScreeningDto> getAvailableScreenings(TimeRangeDto timeRangeDto) {

        return screeningRepository.findByStartTimeBetween(timeRangeDto.getFrom(), timeRangeDto.getTo()).stream()
                .map(screening -> AvailableScreeningDto.builder()
                        .screeningId(screening.getId())
                        .movieTitle(screening.getMovie().getTitle())
                        .startScreeningTime(screening.getStartScreeningTime())
                        .build()
                )
                .collect(Collectors.toList());
    }

}
