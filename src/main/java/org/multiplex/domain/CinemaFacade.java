package org.multiplex.domain;

import java.time.OffsetDateTime;
import java.util.List;

class CinemaFacade {

    private final ScreeningRepository screeningRepository;

    CinemaFacade(ScreeningRepository screeningRepository) {
        this.screeningRepository = screeningRepository;
    }

    List<Screening> findScreenings(OffsetDateTime from, OffsetDateTime to) {

        return screeningRepository.findByStartTimeBetween(from, to);
    }

}
