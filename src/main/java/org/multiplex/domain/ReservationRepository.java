package org.multiplex.domain;

import java.util.List;

interface ReservationRepository {

    Reservation save(Reservation reservation);

    List<Reservation> findByScreeningId(int screeningId);
}
