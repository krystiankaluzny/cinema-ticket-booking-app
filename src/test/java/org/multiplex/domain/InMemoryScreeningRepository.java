package org.multiplex.domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

class InMemoryScreeningRepository implements ScreeningRepository {

    private final Map<Integer, Screening> data = new ConcurrentHashMap<>();

    void add(Screening screening) {
        data.put(screening.getId(), screening);
    }

    public List<Screening> findByStartTimeBetween(OffsetDateTime from, OffsetDateTime to) {
        return data.values().stream()
                .filter(screening -> {
                    final OffsetDateTime startScreeningTime = screening.getStartScreeningTime();
                    return !(startScreeningTime.isBefore(from) || startScreeningTime.isAfter(to));
                })
                .collect(Collectors.toList());
    }

    @Override
    public Screening findById(int screeningId) {
        return data.get(screeningId);
    }
}
