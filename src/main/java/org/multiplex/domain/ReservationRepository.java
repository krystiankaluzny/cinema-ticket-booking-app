package org.multiplex.domain;

import java.util.List;

interface ReservationRepository {

    List<Reservation> findByScreeningId(int screeningId);
}
