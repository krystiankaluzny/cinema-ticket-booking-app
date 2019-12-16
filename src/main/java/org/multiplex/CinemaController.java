package org.multiplex;

import org.multiplex.domain.CinemaService;
import org.multiplex.domain.dto.AvailableScreeningDto;
import org.multiplex.domain.dto.ReservationDto;
import org.multiplex.domain.dto.ReservationSummaryDto;
import org.multiplex.domain.dto.ScreeningIdDto;
import org.multiplex.domain.dto.ScreeningSeatsInfoDto;
import org.multiplex.domain.dto.TimeRangeDto;
import org.multiplex.domain.exception.InvalidUserNameOrSurnameException;
import org.multiplex.domain.exception.NoSeatToReserveException;
import org.multiplex.domain.exception.ReservationTimeException;
import org.multiplex.domain.exception.ScreeningNotFoundException;
import org.multiplex.domain.exception.SeatGapException;
import org.multiplex.domain.exception.SeatNotFoundException;
import org.multiplex.domain.exception.SeatReservedException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
class CinemaController {
    private final CinemaService cinemaService;

    CinemaController(CinemaService cinemaService) {
        this.cinemaService = cinemaService;
    }

    @GetMapping("screenings")
    public List<AvailableScreeningDto> getAvailableScreenings(
            @RequestParam(value = "from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime from,
            @RequestParam(value = "to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime to) {

        TimeRangeDto range = TimeRangeDto.builder()
                .from(from)
                .to(to)
                .build();
        return cinemaService.getAvailableScreenings(range);
    }

    @GetMapping("screening/{id}")
    public ScreeningSeatsInfoDto getScreeningSeatsInfo(@PathVariable("id") int id) {

        try {

            return cinemaService.getScreeningSeatsInfo(ScreeningIdDto.fromInt(id));

        } catch (ScreeningNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @PostMapping("reserve")
    public ReservationSummaryDto reserveSeats(@RequestBody ReservationDto reservationDto) {
        try {

            return cinemaService.reserveSeats(reservationDto);

        } catch (ScreeningNotFoundException | SeatNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (InvalidUserNameOrSurnameException | NoSeatToReserveException | ReservationTimeException | SeatGapException | SeatReservedException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

}
