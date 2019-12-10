package org.multiplex.domain;

import java.time.OffsetDateTime;
import java.util.List;

interface ScreeningRepository {

    Screening findById(int screeningId);

    List<Screening> findByStartTimeBetween(OffsetDateTime from, OffsetDateTime to);
}
