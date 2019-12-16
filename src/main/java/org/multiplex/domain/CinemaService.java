package org.multiplex.domain;

import org.multiplex.domain.dto.AvailableScreeningDto;
import org.multiplex.domain.dto.ReservationDto;
import org.multiplex.domain.dto.ReservationDto.BookingUserDto;
import org.multiplex.domain.dto.ReservationDto.SeatToReserveDto;
import org.multiplex.domain.dto.ReservationSummaryDto;
import org.multiplex.domain.dto.ScreeningIdDto;
import org.multiplex.domain.dto.ScreeningSeatsInfoDto;
import org.multiplex.domain.dto.ScreeningSeatsInfoDto.AvailableSeatDto;
import org.multiplex.domain.dto.TimeRangeDto;
import org.multiplex.domain.exception.ReservationTimeException;
import org.multiplex.domain.exception.ScreeningNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static io.vavr.collection.List.ofAll;
import static io.vavr.collection.List.rangeClosed;
import static java.util.Comparator.comparing;

public class CinemaService {

    private final ScreeningRepository screeningRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationPricingPolicy reservationPricingPolicy;
    private final UserValidator userValidator;
    private final SeatsValidator seatsValidator;
    private final Clock clock;

    CinemaService(ScreeningRepository screeningRepository,
                  ReservationRepository reservationRepository,
                  ReservationPricingPolicy reservationPricingPolicy,
                  UserValidator userValidator,
                  SeatsValidator seatsValidator, Clock clock) {
        this.screeningRepository = screeningRepository;
        this.reservationRepository = reservationRepository;
        this.reservationPricingPolicy = reservationPricingPolicy;
        this.userValidator = userValidator;
        this.seatsValidator = seatsValidator;
        this.clock = clock;
    }

    public List<AvailableScreeningDto> getAvailableScreenings(TimeRangeDto timeRangeDto) {

        return ofAll(screeningRepository.findByStartScreeningTimeBetween(timeRangeDto.getFrom(), timeRangeDto.getTo()))
                .map(screening -> AvailableScreeningDto.builder()
                        .screeningId(screening.getId())
                        .movieTitle(screening.getMovie().getTitle())
                        .startScreeningTime(screening.getStartScreeningTime())
                        .build()
                )
                .sorted(comparing(AvailableScreeningDto::getMovieTitle)
                        .thenComparing(AvailableScreeningDto::getStartScreeningTime))
                .asJava();
    }

    public ScreeningSeatsInfoDto getScreeningSeatsInfo(ScreeningIdDto screeningId) {

        Screening screening = screeningRepository.findById(screeningId.getValue());
        if (screening == null) {
            throw new ScreeningNotFoundException(screeningId.getValue());
        }

        var reservedSeatsInRoom = getReservedSeats(screeningId.getValue());

        var allSeatsInRoom = rangeClosed(1, screening.getRoom().getRowCount())
                .crossProduct(rangeClosed(1, screening.getRoom().getColumnCount()))
                .map(rowColumn -> new Seat(rowColumn._1, rowColumn._2))
                .toList();

        var availableSeats = allSeatsInRoom.removeAll(reservedSeatsInRoom)
                .map(seat -> AvailableSeatDto.builder()
                        .row(seat.getRow())
                        .column(seat.getColumn())
                        .build())
                .asJava();

        return ScreeningSeatsInfoDto.builder()
                .screeningId(screeningId.getValue())
                .roomName(screening.getRoom().getName())
                .availableSeats(availableSeats)
                .build();
    }

    @Transactional
    public ReservationSummaryDto reserveSeats(ReservationDto reservationDto) {

        int screeningId = reservationDto.getScreeningId();
        Screening screening = screeningRepository.findById(screeningId);

        if (screening == null) {
            throw new ScreeningNotFoundException(screeningId);
        }

        if (isReservationTimeInvalid(screening.getStartScreeningTime())) {
            throw new ReservationTimeException();
        }

        BookingUserDto bookingUser = reservationDto.getBookingUser();
        userValidator.validate(bookingUser);

        List<SeatToReserveDto> seatsToReserve = reservationDto.getSeatsToReserve();

        seatsValidator.validate(seatsToReserve, getReservedSeats(screeningId), screening.getRoom());

        OffsetDateTime expirationTime = calculateExpirationTime(screening);

        ReservationPricingPolicy.Price totalPrice = ofAll(seatsToReserve)
                .map(SeatToReserveDto::getReservationType)
                .map(this::typeFromDto)
                .map(reservationPricingPolicy::getPrice)
                .reduce(ReservationPricingPolicy.Price::add);

        Set<ReservedSeat> reservedSeats = ofAll(seatsToReserve)
                .map(seat ->
                        ReservedSeat.builder()
                                .row(seat.getRow())
                                .column(seat.getColumn())
                                .type(typeFromDto(seat.getReservationType()))
                                .build())
                .toJavaSet();

        Reservation reservation = Reservation.builder()
                .screening(screening)
                .bookingUserName(bookingUser.getName())
                .bookingUserSurname(bookingUser.getSurname())
                .expirationTime(expirationTime)
                .reservedSeats(reservedSeats)
                .paid(false)
                .build();

        reservation = reservationRepository.save(reservation);

        return ReservationSummaryDto.builder()
                .reservationId(reservation.getId())
                .expirationTime(expirationTime)
                .totalCost(totalPrice.getValue())
                .build();
    }

    private boolean isReservationActive(Reservation reservation) {
        return reservation.isPaid() || reservation.getExpirationTime().isAfter(OffsetDateTime.now(clock));
    }

    private boolean isReservationTimeInvalid(OffsetDateTime startScreeningTime) {
        return OffsetDateTime.now(clock).plusMinutes(15)
                .isAfter(startScreeningTime);
    }

    private OffsetDateTime calculateExpirationTime(Screening screening) {
        OffsetDateTime proposal = OffsetDateTime.now(clock).plusDays(1);
        if (proposal.isBefore(screening.getStartScreeningTime())) {
            return proposal;
        } else {
            return screening.getStartScreeningTime();
        }
    }

    private io.vavr.collection.List<Seat> getReservedSeats(int screeningId) {

        return ofAll(reservationRepository.findByScreeningId(screeningId))
                .filter(this::isReservationActive)
                .flatMap(Reservation::getReservedSeats)
                .map(reservedSeat -> new Seat(reservedSeat.getRow(), reservedSeat.getColumn()));
    }

    private ReservationType typeFromDto(ReservationDto.ReservationType reservationType) {
        switch (reservationType) {
            case ADULT:
                return ReservationType.ADULT;
            case STUDENT:
                return ReservationType.STUDENT;
            case CHILD:
                return ReservationType.CHILD;
        }

        throw new IllegalArgumentException("Unknown reservation type: " + reservationType);
    }
}
