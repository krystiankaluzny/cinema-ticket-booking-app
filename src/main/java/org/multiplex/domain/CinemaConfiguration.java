package org.multiplex.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class CinemaConfiguration {

    @Bean
    CinemaService cinemaService(ScreeningRepository screeningRepository, ReservationRepository reservationRepository) {

        ReservationPricingPolicy reservationPricingPolicy = new ReservationPricingPolicy();
        UserValidator userValidator = new UserValidator();
        SeatsValidator seatsValidator = new SeatsValidator();
        Clock clock = Clock.systemDefaultZone();

        return new CinemaService(screeningRepository, reservationRepository, reservationPricingPolicy, userValidator, seatsValidator, clock);
    }
}
