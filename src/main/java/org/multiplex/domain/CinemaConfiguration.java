package org.multiplex.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Configuration
class CinemaConfiguration {

    @Bean
    CinemaService cinemaService(
            ScreeningRepository screeningRepository,
            ReservationRepository reservationRepository,
            RoomRepository roomRepository,
            MovieRepository movieRepository) {

        addTestData(screeningRepository, roomRepository, movieRepository);

        ReservationPricingPolicy reservationPricingPolicy = new ReservationPricingPolicy();
        UserValidator userValidator = new UserValidator();
        SeatsValidator seatsValidator = new SeatsValidator();
        Clock clock = Clock.systemDefaultZone();

        return new CinemaService(screeningRepository, reservationRepository, reservationPricingPolicy, userValidator, seatsValidator, clock);
    }

    private void addTestData(ScreeningRepository screeningRepository, RoomRepository roomRepository, MovieRepository movieRepository) {

        Movie titanic = movieRepository.save(new Movie(0,"Titanic", Duration.ofMinutes(194)));
        Movie gladiator = movieRepository.save(new Movie(0, "Gladiator", Duration.ofMinutes(171)));
        Movie forrestGump = movieRepository.save(new Movie(0, "Forrest Gump", Duration.ofMinutes(141)));

        Room redRoom = roomRepository.save(new Room(0, "Sala czerwona", 20, 20));
        Room blueRoom = roomRepository.save(new Room(0, "Sala niebieska", 5, 10));
        Room yellowRoom = roomRepository.save(new Room(0, "Sala żółta", 25, 25));

        screeningRepository.save(new Screening(0, titanic, redRoom, date("2019-12-15", "09:00")));
        screeningRepository.save(new Screening(0, gladiator, redRoom, date("2019-12-15", "12:30")));
        screeningRepository.save(new Screening(0, forrestGump, redRoom, date("2019-12-15", "15:30")));
        screeningRepository.save(new Screening(0, titanic, redRoom, date("2019-12-15", "18:30")));
        screeningRepository.save(new Screening(0, gladiator, blueRoom, date("2019-12-15", "09:00")));
        screeningRepository.save(new Screening(0, forrestGump, blueRoom, date("2019-12-15", "12:00")));

        screeningRepository.save(new Screening(0, titanic, blueRoom, date("2019-12-16", "14:30")));
        screeningRepository.save(new Screening(0, gladiator, blueRoom, date("2019-12-16", "18:00")));
        screeningRepository.save(new Screening(0, forrestGump, yellowRoom, date("2019-12-16", "09:30")));
        screeningRepository.save(new Screening(0, titanic, yellowRoom, date("2019-12-16", "12:00")));
        screeningRepository.save(new Screening(0, gladiator, yellowRoom, date("2019-12-16", "16:00")));
        screeningRepository.save(new Screening(0, forrestGump, yellowRoom, date("2019-12-16", "19:00")));
    }

    private OffsetDateTime date(String date, String time) {
        return OffsetDateTime.of(LocalDate.parse(date), LocalTime.parse(time), ZoneOffset.UTC);
    }
}
