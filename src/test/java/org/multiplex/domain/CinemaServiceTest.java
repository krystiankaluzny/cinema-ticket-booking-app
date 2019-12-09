package org.multiplex.domain;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.multiplex.domain.dto.AvailableScreeningDto;
import org.multiplex.domain.dto.ReservationDto;
import org.multiplex.domain.dto.ReservationDto.SeatToReserveDto;
import org.multiplex.domain.dto.ReservationSummaryDto;
import org.multiplex.domain.dto.ScreeningIdDto;
import org.multiplex.domain.dto.ScreeningSeatsInfoDto;
import org.multiplex.domain.dto.TimeRangeDto;
import org.multiplex.domain.exception.ScreeningNotFoundException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.multiplex.domain.Screening.Movie;
import static org.multiplex.domain.Screening.Room;

class CinemaServiceTest {

    private final InMemoryScreeningRepository screeningRepo = new InMemoryScreeningRepository();
    private final InMemoryReservationRepository reservationRepo = new InMemoryReservationRepository();
    private final ReservationPricingPolicy reservationPricingPolicy = new StandardPricingPolicy();
    private final Clock clock = Clock.fixed(Instant.parse("2019-12-09T10:30:02.00Z"), ZoneOffset.UTC);
    private final CinemaService cinemaService = new CinemaService(screeningRepo, reservationRepo, reservationPricingPolicy, clock);

    @Test
    public void getAvailableScreenings_ReturnsScreensInTimeRange() {

        //given
        addScreening(TITANIC, YELLOW_ROOM, date("2019-12-05", "09:00"));

        addScreening(TITANIC, YELLOW_ROOM, date("2019-12-06", "09:00"));
        addScreening(TITANIC, BLUE_ROOM, date("2019-12-06", "12:00"));
        addScreening(GLADIATOR, YELLOW_ROOM, date("2019-12-06", "13:00"));
        addScreening(GLADIATOR, RED_ROOM, date("2019-12-06", "18:00"));

        TimeRangeDto timeRangeDto = TimeRangeDto.builder()
                .from(date("2019-12-06", "08:00"))
                .to(date("2019-12-06", "16:00"))
                .build();

        //when
        List<AvailableScreeningDto> availableScreenings = cinemaService.getAvailableScreenings(timeRangeDto);

        then(availableScreenings).hasSize(3)
                .haveExactly(1, screening(TITANIC, date("2019-12-06", "09:00")))
                .haveExactly(1, screening(TITANIC, date("2019-12-06", "12:00")))
                .haveExactly(1, screening(GLADIATOR, date("2019-12-06", "13:00")));
    }


    @Test
    public void getScreeningSeatsInfo_ReturnsAllSeatsAsAvailable_IfThereIsNoReservation() {

        //given
        int screeningId = addScreening(FORREST_GUMP, RED_ROOM, date("2019-12-09", "12:30"));
        ScreeningIdDto id = ScreeningIdDto.fromInt(screeningId);

        //when
        ScreeningSeatsInfoDto screeningSeatsInfo = cinemaService.getScreeningSeatsInfo(id);

        then(screeningSeatsInfo.getRoomName()).isEqualTo(RED_ROOM.getName());
        then(screeningSeatsInfo.getAvailableSeats()).hasSize(400);
    }

    @Test
    public void getScreeningSeatsInfo_ReturnsAvailableSeats_SeatsForPaidReservationAreSkipped() {

        //given
        int screeningId = addScreening(FORREST_GUMP, RED_ROOM, date("2019-12-09", "12:30"));
        ScreeningIdDto id = ScreeningIdDto.fromInt(screeningId);

        Set<ReservedSeat> reservedSeats = Set.of(
                new ReservedSeat(3, 10, ReservationType.ADULT),
                new ReservedSeat(3, 13, ReservationType.ADULT),
                new ReservedSeat(10, 3, ReservationType.ADULT));

        Reservation.ReservationBuilder reservationBuilder = Reservation.builder()
                .screeningId(screeningId)
                .reservedSeats(reservedSeats)
                .paid(true);

        addReservation(reservationBuilder);

        //when
        ScreeningSeatsInfoDto screeningSeatsInfo = cinemaService.getScreeningSeatsInfo(id);

        then(screeningSeatsInfo.getRoomName()).isEqualTo(RED_ROOM.getName());
        then(screeningSeatsInfo.getAvailableSeats()).hasSize(397);
    }

    @Test
    public void getScreeningSeatsInfo_ReturnsAvailableSeats_SeatsForNoPaidAndNoExpiredReservationAreSkipped() {

        //given
        int screeningId = addScreening(FORREST_GUMP, YELLOW_ROOM, date("2019-12-09", "12:30"));
        ScreeningIdDto id = ScreeningIdDto.fromInt(screeningId);

        Set<ReservedSeat> reservedSeats = Set.of(
                new ReservedSeat(3, 10, ReservationType.ADULT),
                new ReservedSeat(10, 3, ReservationType.ADULT));

        Reservation.ReservationBuilder reservationBuilder = Reservation.builder()
                .screeningId(screeningId)
                .reservedSeats(reservedSeats)
                .expirationTime(OffsetDateTime.now(clock).plusHours(2))
                .paid(false);

        addReservation(reservationBuilder);

        //when
        ScreeningSeatsInfoDto screeningSeatsInfo = cinemaService.getScreeningSeatsInfo(id);

        then(screeningSeatsInfo.getRoomName()).isEqualTo(YELLOW_ROOM.getName());
        then(screeningSeatsInfo.getAvailableSeats()).hasSize(623);
    }

    @Test
    public void getScreeningSeatsInfo_ReturnsAvailableSeats_SeatsForNoPaidAndExpiredReservationAreAvailable() {

        //given
        int screeningId = addScreening(FORREST_GUMP, BLUE_ROOM, date("2019-12-09", "12:30"));
        ScreeningIdDto id = ScreeningIdDto.fromInt(screeningId);

        Set<ReservedSeat> reservedSeats = Set.of(
                new ReservedSeat(3, 10, ReservationType.ADULT),
                new ReservedSeat(23, 3, ReservationType.ADULT));

        Reservation.ReservationBuilder reservationBuilder = Reservation.builder()
                .screeningId(screeningId)
                .reservedSeats(reservedSeats)
                .expirationTime(OffsetDateTime.now(clock).minusHours(2))
                .paid(false);

        addReservation(reservationBuilder);

        //when
        ScreeningSeatsInfoDto screeningSeatsInfo = cinemaService.getScreeningSeatsInfo(id);

        then(screeningSeatsInfo.getRoomName()).isEqualTo(BLUE_ROOM.getName());
        then(screeningSeatsInfo.getAvailableSeats()).hasSize(540);
    }

    @Test
    public void getScreeningSeatsInfo_Throws_IfThereIsNotScreeningWithGivenId() {

        //given
        ScreeningIdDto id = ScreeningIdDto.fromInt(17);

        thenThrownBy(() -> cinemaService.getScreeningSeatsInfo(id))
                .isInstanceOf(ScreeningNotFoundException.class);
    }

    @Test
    public void reserveSeats_CalculateCost_ForThreeReservationTypes_And_GiveExpirationTime() {

        //given
        int screeningId = addScreening(GLADIATOR, YELLOW_ROOM, date("2019-12-10", "10:30"));

        SeatToReserveDto adultSeat = SeatToReserveDto.builder()
                .row(1)
                .column(1)
                .reservationType(ReservationDto.ReservationType.ADULT)
                .build();

        SeatToReserveDto studentSeat = SeatToReserveDto.builder()
                .row(1)
                .column(2)
                .reservationType(ReservationDto.ReservationType.STUDENT)
                .build();

        SeatToReserveDto childSeat = SeatToReserveDto.builder()
                .row(1)
                .column(3)
                .reservationType(ReservationDto.ReservationType.CHILD)
                .build();

        ReservationDto reservationDto = ReservationDto.builder()
                .screeningId(ScreeningIdDto.fromInt(screeningId))
                .seatsToReserve(List.of(adultSeat, studentSeat, childSeat))
                .bookingUser(ReservationDto.BookingUserDto.builder()
                        .name("John")
                        .surname("Smith")
                        .build())
                .build();

        //when
        ReservationSummaryDto reservationSummary = cinemaService.reserveSeats(reservationDto);

        then(reservationSummary.getTotalCost()).isEqualByComparingTo("55.50");
        then(reservationSummary.getExpirationTime()).isNotNull();

    }

    @Test
    public void reserveSeats_Throws__IfThereIsNotScreeningWithGivenId() {

        //given
        ReservationDto reservationDto = ReservationDto.builder()
                .screeningId(ScreeningIdDto.fromInt(20))
                .build();

        thenThrownBy(() -> cinemaService.reserveSeats(reservationDto))
                .isInstanceOf(ScreeningNotFoundException.class);

    }

    private OffsetDateTime date(String date, String time) {
        return OffsetDateTime.of(LocalDate.parse(date), LocalTime.parse(time), ZoneOffset.UTC);
    }

    private static int nextScreeningId = 1;

    private int addScreening(Movie movie, Room room, OffsetDateTime startTime) {
        int screeningId = nextScreeningId++;
        screeningRepo.add(new Screening(screeningId, movie, room, startTime));

        return screeningId;
    }

    private static int nextReservationId = 1;

    private int addReservation(Reservation.ReservationBuilder builder) {

        int reservationId = nextReservationId++;

        reservationRepo.save(builder.id(reservationId).build());

        return reservationId;
    }

    private Condition<AvailableScreeningDto> screening(Movie movie, OffsetDateTime startTime) {
        return new Condition<>(availableScreening ->
                availableScreening.getMovieTitle().equals(movie.getTitle())
                        && availableScreening.getStartScreeningTime().equals(startTime),
                "Screening condition");
    }

    private static final Movie TITANIC = new Movie(1, "Titanic", Duration.ofMinutes(194));
    private static final Movie GLADIATOR = new Movie(2, "Gladiator", Duration.ofMinutes(171));
    private static final Movie FORREST_GUMP = new Movie(3, "Forrest Gump", Duration.ofMinutes(141));

    private static final Room RED_ROOM = new Room(1, "Sala czerwona", 20, 20);
    private static final Room BLUE_ROOM = new Room(2, "Sala niebieska", 18, 30);
    private static final Room YELLOW_ROOM = new Room(3, "Sala żółta", 25, 25);
}