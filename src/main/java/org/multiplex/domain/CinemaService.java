package org.multiplex.domain;

import org.multiplex.domain.dto.AvailableScreeningDto;
import org.multiplex.domain.dto.ScreeningIdDto;
import org.multiplex.domain.dto.ScreeningSeatsInfoDto;
import org.multiplex.domain.dto.ScreeningSeatsInfoDto.AvailableSeatDto;
import org.multiplex.domain.dto.TimeRangeDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CinemaService {

    private final ScreeningRepository screeningRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationPricingPolicy reservationPricingPolicy;

    CinemaService(ScreeningRepository screeningRepository, ReservationRepository reservationRepository, ReservationPricingPolicy reservationPricingPolicy) {
        this.screeningRepository = screeningRepository;
        this.reservationRepository = reservationRepository;
        this.reservationPricingPolicy = reservationPricingPolicy;
    }

    public List<AvailableScreeningDto> getAvailableScreenings(TimeRangeDto timeRangeDto) {

        return screeningRepository.findByStartTimeBetween(timeRangeDto.getFrom(), timeRangeDto.getTo()).stream()
                .map(screening -> AvailableScreeningDto.builder()
                        .screeningId(ScreeningIdDto.builder()
                                .value(screening.getId())
                                .build())
                        .movieTitle(screening.getMovie().getTitle())
                        .startScreeningTime(screening.getStartScreeningTime())
                        .build()
                )
                .collect(Collectors.toList());
    }

    public ScreeningSeatsInfoDto getScreeningSeatsInfo(ScreeningIdDto screeningId) {

        Screening screening = screeningRepository.findById(screeningId.getValue());
        List<Reservation> reservations = reservationRepository.findByScreeningId(screeningId.getValue());

        Map<Integer, Set<Integer>> reservedSeats= new HashMap<>();

        for (Reservation reservation : reservations) {
            Set<Integer> reservedColumnsInRow = reservedSeats.computeIfAbsent(reservation.getRow(), row -> new HashSet<>());
            reservedColumnsInRow.add(reservation.getColumn());
        }
        
        List<AvailableSeatDto> availableSeats = new ArrayList<>();
        
        for (int row = 0; row < screening.getRoom().getRowCount(); row++) {
            for (int col = 0; col < screening.getRoom().getColumnCount(); col++) {
                
                Set<Integer> reservedColumnsInRow = reservedSeats.get(row);

                if(reservedColumnsInRow == null || !reservedColumnsInRow.contains(col)) {

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
}
