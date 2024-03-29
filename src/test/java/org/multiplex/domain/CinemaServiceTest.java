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
import org.multiplex.domain.exception.ReservationTimeException;
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
import static org.assertj.core.api.BDDAssertions.thenCode;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

class CinemaServiceTest {

    private final InMemoryScreeningRepository screeningRepo = new InMemoryScreeningRepository();
    private final InMemoryReservationRepository reservationRepo = new InMemoryReservationRepository();
    private final ReservationPricingPolicy reservationPricingPolicy = new ReservationPricingPolicy();
    private final UserValidator userValidator = new UserValidator();
    private final SeatsValidator seatsValidator = new SeatsValidator();
    private final Clock clock = Clock.fixed(Instant.parse("2019-12-09T10:30:02.00Z"), ZoneOffset.UTC);
    private final CinemaService cinemaService = new CinemaService(screeningRepo, reservationRepo, reservationPricingPolicy, userValidator, seatsValidator, clock);

    @Test
    public void getAvailableScreenings_ReturnsScreenings_InTimeRange() {

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
    public void getAvailableScreenings_ReturnsSortedScreenings_ByMovieTitleAndScreeningTime() {

        //given
        addScreening(TITANIC, RED_ROOM, date("2019-12-06", "10:00"));
        addScreening(TITANIC, YELLOW_ROOM, date("2019-12-06", "09:00"));
        addScreening(TITANIC, BLUE_ROOM, date("2019-12-06", "12:00"));
        addScreening(GLADIATOR, RED_ROOM, date("2019-12-06", "18:00"));
        addScreening(GLADIATOR, YELLOW_ROOM, date("2019-12-06", "13:00"));

        TimeRangeDto timeRangeDto = TimeRangeDto.builder()
                .from(date("2019-12-06", "08:00"))
                .to(date("2019-12-06", "20:00"))
                .build();

        //when
        List<AvailableScreeningDto> availableScreenings = cinemaService.getAvailableScreenings(timeRangeDto);

        then(availableScreenings).hasSize(5);
        then(availableScreenings).element(0).is(screening(GLADIATOR, date("2019-12-06", "13:00")));
        then(availableScreenings).element(1).is(screening(GLADIATOR, date("2019-12-06", "18:00")));
        then(availableScreenings).element(2).is(screening(TITANIC, date("2019-12-06", "09:00")));
        then(availableScreenings).element(3).is(screening(TITANIC, date("2019-12-06", "10:00")));
        then(availableScreenings).element(4).is(screening(TITANIC, date("2019-12-06", "12:00")));
    }


    @Test
    public void getScreeningSeatsInfo_ReturnsAllSeatsAsAvailable_IfThereIsNoReservation() {

        //given
        int screeningId = addScreening(FORREST_GUMP, RED_ROOM, date("2019-12-09", "12:30")).getId();
        ScreeningIdDto id = ScreeningIdDto.fromInt(screeningId);

        //when
        ScreeningSeatsInfoDto screeningSeatsInfo = cinemaService.getScreeningSeatsInfo(id);

        then(screeningSeatsInfo.getRoomName()).isEqualTo(RED_ROOM.getName());
        then(screeningSeatsInfo.getAvailableSeats()).hasSize(400);
    }

    @Test
    public void getScreeningSeatsInfo_ReturnsAvailableSeats_SeatsForPaidReservationAreSkipped() {

        //given
        Screening screening = addScreening(FORREST_GUMP, RED_ROOM, date("2019-12-09", "12:30"));
        ScreeningIdDto id = ScreeningIdDto.fromInt(screening.getId());

        Set<ReservedSeat> reservedSeats = Set.of(
                reservedSeat(3, 10),
                reservedSeat(3, 13),
                reservedSeat(10, 3));

        Reservation.ReservationBuilder reservationBuilder = Reservation.builder()
                .screening(screening)
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
        Screening screening = addScreening(FORREST_GUMP, YELLOW_ROOM, date("2019-12-09", "12:30"));
        ScreeningIdDto id = ScreeningIdDto.fromInt(screening.getId());

        Set<ReservedSeat> reservedSeats = Set.of(
                reservedSeat(3, 10),
                reservedSeat(10, 3));

        Reservation.ReservationBuilder reservationBuilder = Reservation.builder()
                .screening(screening)
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
        Screening screening = addScreening(FORREST_GUMP, BLUE_ROOM, date("2019-12-09", "12:30"));
        ScreeningIdDto id = ScreeningIdDto.fromInt(screening.getId());

        Set<ReservedSeat> reservedSeats = Set.of(
                reservedSeat(3, 10),
                reservedSeat(23, 3));

        Reservation.ReservationBuilder reservationBuilder = Reservation.builder()
                .screening(screening)
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
        int screeningId = addScreening(GLADIATOR, YELLOW_ROOM, date("2019-12-10", "10:30")).getId();

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
                .screeningId(screeningId)
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
    public void reserveSeats_Pass_IfThereIsMoreOrEqualThan15MinutesToScreeningTime() {

        //given
        OffsetDateTime screeningStartTime = OffsetDateTime.now(clock).plusMinutes(15);
        int screeningId = addScreening(FORREST_GUMP, RED_ROOM, screeningStartTime).getId();

        SeatToReserveDto adultSeat = SeatToReserveDto.builder()
                .row(1)
                .column(1)
                .reservationType(ReservationDto.ReservationType.ADULT)
                .build();

        ReservationDto reservationDto = ReservationDto.builder()
                .screeningId(screeningId)
                .seatsToReserve(List.of(adultSeat))
                .bookingUser(ReservationDto.BookingUserDto.builder()
                        .name("John")
                        .surname("Smith")
                        .build())
                .build();

        //when
        thenCode(() -> cinemaService.reserveSeats(reservationDto))
                .doesNotThrowAnyException();
    }

    @Test
    public void reserveSeats_Throws_IfThereIsLessThan15MinutesToScreeningTime() {

        //given
        OffsetDateTime screeningStartTime = OffsetDateTime.now(clock).plusMinutes(14);
        int screeningId = addScreening(FORREST_GUMP, RED_ROOM, screeningStartTime).getId();

        ReservationDto reservationDto = ReservationDto.builder()
                .screeningId(screeningId)
                .build();

        //when
        thenThrownBy(() -> cinemaService.reserveSeats(reservationDto))
                .isInstanceOf(ReservationTimeException.class);
    }

    @Test
    public void reserveSeats_Throws_IfThereIsNotScreeningWithGivenId() {

        //given
        ReservationDto reservationDto = ReservationDto.builder()
                .screeningId(20)
                .build();

        thenThrownBy(() -> cinemaService.reserveSeats(reservationDto))
                .isInstanceOf(ScreeningNotFoundException.class);

    }

    private OffsetDateTime date(String date, String time) {
        return OffsetDateTime.of(LocalDate.parse(date), LocalTime.parse(time), ZoneOffset.UTC);
    }

    private static int nextScreeningId = 1;

    private Screening addScreening(Movie movie, Room room, OffsetDateTime startTime) {
        int screeningId = nextScreeningId++;
        Screening screening = new Screening(screeningId, movie, room, startTime);
        screeningRepo.add(screening);

        return screening;
    }

    private static int nextReservationId = 1;

    private int addReservation(Reservation.ReservationBuilder builder) {

        int reservationId = nextReservationId++;

        reservationRepo.save(builder.id(reservationId).build());

        return reservationId;
    }


    private ReservedSeat reservedSeat(int row, int collumn) {
        return new ReservedSeat(0, row, collumn, ReservationType.ADULT);
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