package org.multiplex.domain;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class CinemaServiceTest {

    private final InMemoryScreeningRepository repository = new InMemoryScreeningRepository();
    private final CinemaService cinemaService = new CinemaService(repository);

    @Test
    public void shouldReturnScreeningsStartsInGivenTimeRange() {

        //given
        addScreening(TITANIC, YELLOW_ROOM, date("2019-12-05", "09:00"));

        addScreening(TITANIC, YELLOW_ROOM, date("2019-12-06", "09:00"));
        addScreening(TITANIC, BLUE_ROOM, date("2019-12-06", "12:00"));
        addScreening(GLADIATOR, YELLOW_ROOM, date("2019-12-06", "13:00"));
        addScreening(GLADIATOR, RED_ROOM, date("2019-12-06", "18:00"));

        OffsetDateTime from = date("2019-12-06", "08:00");
        OffsetDateTime to = date("2019-12-06", "16:00");

        //when
        final List<Screening> screenings = cinemaService.findScreenings(from, to);

        then(screenings).hasSize(3)
                .haveExactly(1, screening(TITANIC, YELLOW_ROOM, date("2019-12-06", "09:00")))
                .haveExactly(1, screening(TITANIC, BLUE_ROOM, date("2019-12-06", "12:00")))
                .haveExactly(1, screening(GLADIATOR, YELLOW_ROOM, date("2019-12-06", "13:00")));
    }

    private OffsetDateTime date(String date, String time) {
        return OffsetDateTime.of(LocalDate.parse(date), LocalTime.parse(time), ZoneOffset.UTC);
    }

    private static int nextScreeningId = 1;

    private void addScreening(Movie movie, Room room, OffsetDateTime startTime) {
        repository.add(new Screening(nextScreeningId++, movie, room, startTime));
    }

    private Condition<Screening> screening(Movie movie, Room room, OffsetDateTime startTime) {
        return new Condition<>(screening ->
                screening.getMovie().equals(movie)
                        && screening.getRoom().equals(room)
                        && screening.getStartScreeningTime().equals(startTime),
                "Screening condition");
    }

    private static final Movie TITANIC = new Movie(1, "Titanic", Duration.ofMinutes(194));
    private static final Movie GLADIATOR = new Movie(2, "Gladiator", Duration.ofMinutes(171));
    private static final Movie FORREST_GUMP = new Movie(3, "Forrest Gump", Duration.ofMinutes(141));

    private static final Room RED_ROOM = new Room(1, "Sala czerwona", 20, 20);
    private static final Room BLUE_ROOM = new Room(2, "Sala niebieska", 18, 30);
    private static final Room YELLOW_ROOM = new Room(3, "Sala żółta", 25, 25);
}