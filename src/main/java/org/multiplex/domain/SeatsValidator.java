package org.multiplex.domain;

import org.multiplex.domain.dto.ReservationDto;
import org.multiplex.domain.exception.SeatGapException;
import org.multiplex.domain.exception.SeatNotFoundException;
import org.multiplex.domain.exception.SeatReservedException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class SeatsValidator {

    public void validate(List<ReservationDto.SeatToReserveDto> seatsToReserve, Map<Integer, Set<Integer>> reservedSeats, Screening.Room room) {

        for (ReservationDto.SeatToReserveDto seatToReserve : seatsToReserve) {
            int row = seatToReserve.getRow();
            int column = seatToReserve.getColumn();

            if (row < 1 || row > room.getRowCount()
                    || column < 1 || column > room.getColumnCount()) {
                throw new SeatNotFoundException(row, column, room.getName());
            }

            Set<Integer> reservedColumnsInRow = reservedSeats.computeIfAbsent(row, i -> new HashSet<>());

            if (reservedColumnsInRow.contains(column)) {
                throw new SeatReservedException(row, column);
            }

            if (column > 2
                    && reservedColumnsInRow.contains(column - 2)
                    && !reservedColumnsInRow.contains(column - 1)) {
                throw new SeatGapException(row, column);
            }

            if (column < room.getColumnCount() - 1
                    && reservedColumnsInRow.contains(column + 2)
                    && !reservedColumnsInRow.contains(column + 1)
            ) {
                throw new SeatGapException(row, column);
            }

            reservedColumnsInRow.add(column);
        }
    }
}
