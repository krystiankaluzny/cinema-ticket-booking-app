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
import org.multiplex.domain.exception.InvalidUserNameOrSurnameException;
import org.multiplex.domain.exception.ReservationTimeException;
import org.multiplex.domain.exception.ScreeningNotFoundException;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        return screeningRepository.findByStartTimeBetween(timeRangeDto.getFrom(), timeRangeDto.getTo()).stream()
                .map(screening -> AvailableScreeningDto.builder()
                        .screeningId(ScreeningIdDto.fromInt(screening.getId()))
                        .movieTitle(screening.getMovie().getTitle())
                        .startScreeningTime(screening.getStartScreeningTime())
                        .build()
                )
                .sorted(comparing(AvailableScreeningDto::getMovieTitle)
                        .thenComparing(AvailableScreeningDto::getStartScreeningTime))
                .collect(Collectors.toList());
    }

    public ScreeningSeatsInfoDto getScreeningSeatsInfo(ScreeningIdDto screeningId) {

        Screening screening = screeningRepository.findById(screeningId.getValue());
        if (screening == null) {
            throw new ScreeningNotFoundException(screeningId.getValue());
        }

        List<Reservation> reservations = reservationRepository.findByScreeningId(screeningId.getValue());

        Map<Integer, Set<Integer>> reservedSeats = new HashMap<>();

        for (Reservation reservation : reservations) {
            if (isReservationActive(reservation)) {
                Set<ReservedSeat> seats = reservation.getReservedSeats();
                for (ReservedSeat seat : seats) {
                    Set<Integer> reservedColumnsInRow = reservedSeats.computeIfAbsent(seat.getRow(), row -> new HashSet<>());
                    reservedColumnsInRow.add(seat.getColumn());
                }
            }
        }

        List<AvailableSeatDto> availableSeats = new ArrayList<>();

        for (int row = 0; row < screening.getRoom().getRowCount(); row++) {
            for (int col = 0; col < screening.getRoom().getColumnCount(); col++) {

                Set<Integer> reservedColumnsInRow = reservedSeats.get(row);

                if (reservedColumnsInRow == null || !reservedColumnsInRow.contains(col)) {

                    availableSeats.add(AvailableSeatDto.builder()
                            .row(row)
                            .column(col)
                            .build());
                }
            }
        }

        return ScreeningSeatsInfoDto.builder()
                .screeningId(screeningId)
                .roomName(screening.getRoom().getName())
                .availableSeats(availableSeats)
                .build();
    }

    public ReservationSummaryDto reserveSeats(ReservationDto reservationDto) {

        int screeningId = reservationDto.getScreeningId().getValue();
        Screening screening = screeningRepository.findById(screeningId);

        if (screening == null) {
            throw new ScreeningNotFoundException(screeningId);
        }

        if (isReservationTimeInvalid(screening.getStartScreeningTime())) {
            throw new ReservationTimeException();
        }

        BookingUserDto bookingUser = reservationDto.getBookingUser();
        if (userValidator.isInvalid(bookingUser.getName(), bookingUser.getSurname())) {
            throw new InvalidUserNameOrSurnameException(bookingUser.getName(), bookingUser.getSurname());
        }

        List<SeatToReserveDto> seatsToReserve = reservationDto.getSeatsToReserve();

        OffsetDateTime expirationTime = OffsetDateTime.now().plusMinutes(15);
        ReservationPricingPolicy.Price totalPrice = ReservationPricingPolicy.Price.ZERO;

        Set<ReservedSeat> reservedSeats = new HashSet<>();
        for (SeatToReserveDto seatToReserveDto : seatsToReserve) {

            ReservationType type = typeFromDto(seatToReserveDto.getReservationType());

            reservedSeats.add(ReservedSeat.builder()
                    .row(seatToReserveDto.getRow())
                    .column(seatToReserveDto.getColumn())
                    .type(type)
                    .build());

            ReservationPricingPolicy.Price price = reservationPricingPolicy.getPrice(type);

            totalPrice.add(price);
        }

        Reservation reservation = Reservation.builder()
                .screeningId(screeningId)
                .bookingUserName(bookingUser.getName())
                .bookingUserSurname(bookingUser.getSurname())
                .expirationTime(expirationTime)
                .reservedSeats(reservedSeats)
                .paid(false)
                .build();

        reservationRepository.save(reservation);

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
