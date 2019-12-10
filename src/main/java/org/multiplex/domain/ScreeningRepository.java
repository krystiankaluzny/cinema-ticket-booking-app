package org.multiplex.domain;

import org.springframework.data.repository.Repository;

import java.time.OffsetDateTime;
import java.util.List;

interface ScreeningRepository extends Repository<Screening, Integer> {

    Screening save(Screening screening);

    Screening findById(int screeningId);

    List<Screening> findByStartScreeningTimeBetween(OffsetDateTime from, OffsetDateTime to);
}
