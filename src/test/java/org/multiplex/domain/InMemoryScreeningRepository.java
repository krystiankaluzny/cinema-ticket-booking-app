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

    @Override
    public Screening save(Screening screening) {
        add(screening);
        return screening;
    }

    @Override
    public Screening findById(int screeningId) {
        return data.get(screeningId);
    }

    @Override
    public List<Screening> findByStartScreeningTimeBetween(OffsetDateTime from, OffsetDateTime to) {
        return data.values().stream()
                .filter(screening -> {
                    final OffsetDateTime startScreeningTime = screening.getStartScreeningTime();
                    return !(startScreeningTime.isBefore(from) || startScreeningTime.isAfter(to));
                })
                .collect(Collectors.toList());
    }
}
