package org.multiplex.domain;

import org.springframework.data.repository.Repository;

import java.util.List;

interface ReservationRepository extends Repository<Reservation, Integer> {

    Reservation save(Reservation reservation);

    List<Reservation> findByScreeningId(int screeningId);
}
