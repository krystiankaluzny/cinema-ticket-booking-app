package org.multiplex.domain;

import java.time.OffsetDateTime;
import java.util.List;

public class CinemaService {

    private final ScreeningRepository screeningRepository;

    CinemaService(ScreeningRepository screeningRepository) {
        this.screeningRepository = screeningRepository;
    }

    List<Screening> findScreenings(OffsetDateTime from, OffsetDateTime to) {

        return screeningRepository.findByStartTimeBetween(from, to);
    }

}
