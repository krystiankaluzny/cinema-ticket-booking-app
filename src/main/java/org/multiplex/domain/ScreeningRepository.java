package org.multiplex.domain;

import java.time.OffsetDateTime;
import java.util.List;

interface ScreeningRepository {
    List<Screening> findByStartTimeBetween(OffsetDateTime from, OffsetDateTime to);
}
