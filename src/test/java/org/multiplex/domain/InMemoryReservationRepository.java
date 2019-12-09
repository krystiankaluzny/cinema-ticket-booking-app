package org.multiplex.domain;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

class InMemoryReservationRepository implements ReservationRepository {

    private final Map<Integer, Reservation> data = new ConcurrentHashMap<>();

    void add(Reservation reservation) {
        data.put(reservation.getId(), reservation);
    }


    @Override
    public List<Reservation> findByScreeningId(int screeningId) {
        return data.values().stream()
                .filter(reservation -> reservation.getScreeningId() == screeningId)
                .collect(Collectors.toList());
    }
}
